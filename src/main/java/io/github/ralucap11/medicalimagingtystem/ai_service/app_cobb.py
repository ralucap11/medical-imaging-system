"""
Medical Imaging AI — Microserviciu 2
Model : YOLOv8s ONNX  (detectie vertebre + calcul unghi Cobb)
Port  : 5002

Endpoints:
  GET  /health          → status
  POST /predict-cobb    → { cobbAngle, severity, vertebraeCount,
                             vertebrae, visualization (base64 PNG) }
"""

import io
import math
import base64
import traceback
import numpy as np
from PIL import Image, ImageDraw
import onnxruntime as ort
from flask import Flask, request, jsonify

app = Flask(__name__)


ONNX_PATH   = "vertebrae_detector.onnx"
INPUT_SIZE  = 640
CONF_THRESH = 0.25
IOU_THRESH  = 0.45

# Clasificare unghi Cobb (standard medical Cobb classification)
def cobb_to_severity(angle: float) -> str:
    if angle < 10:   return "Normal"
    if angle < 25:   return "Mild"
    if angle < 40:   return "Moderate"
    return "Severe"


try:
    session = ort.InferenceSession(
        ONNX_PATH,
        providers=["CUDAExecutionProvider", "CPUExecutionProvider"]
    )
    MODEL_OK = True
    print(f"[OK] YOLOv8 ONNX loaded")
    print(f"     Provider: {session.get_providers()[0]}")
except Exception as e:
    session  = None
    MODEL_OK = False
    print(f"[ERR] YOLOv8 load failed: {e}")


def preprocess_image(img: Image.Image):

    orig_w, orig_h = img.size
    scale = min(INPUT_SIZE / orig_w, INPUT_SIZE / orig_h)
    new_w = int(orig_w * scale)
    new_h = int(orig_h * scale)

    resized = img.resize((new_w, new_h), Image.BILINEAR)


    canvas = Image.new("RGB", (INPUT_SIZE, INPUT_SIZE), (114, 114, 114))
    pad_x  = (INPUT_SIZE - new_w) // 2
    pad_y  = (INPUT_SIZE - new_h) // 2
    canvas.paste(resized, (pad_x, pad_y))

    arr = np.array(canvas, dtype=np.float32) / 255.0          # [H,W,3]
    arr = arr.transpose(2, 0, 1)[np.newaxis]                    # [1,3,H,W]

    meta = {"scale": scale, "pad_x": pad_x, "pad_y": pad_y,
            "orig_w": orig_w, "orig_h": orig_h}
    return arr, meta


def nms(boxes_xywh, scores, iou_thresh):
    """Non-maximum suppression pe boxes [x_c,y_c,w,h]."""
    if len(boxes_xywh) == 0:
        return []

    x1 = boxes_xywh[:, 0] - boxes_xywh[:, 2] / 2
    y1 = boxes_xywh[:, 1] - boxes_xywh[:, 3] / 2
    x2 = boxes_xywh[:, 0] + boxes_xywh[:, 2] / 2
    y2 = boxes_xywh[:, 1] + boxes_xywh[:, 3] / 2
    areas = (x2 - x1) * (y2 - y1)

    order  = scores.argsort()[::-1]
    keep   = []

    while len(order):
        i = order[0]
        keep.append(i)
        xx1 = np.maximum(x1[i], x1[order[1:]])
        yy1 = np.maximum(y1[i], y1[order[1:]])
        xx2 = np.minimum(x2[i], x2[order[1:]])
        yy2 = np.minimum(y2[i], y2[order[1:]])
        inter = np.maximum(0, xx2 - xx1) * np.maximum(0, yy2 - yy1)
        iou   = inter / (areas[i] + areas[order[1:]] - inter + 1e-9)
        order = order[1:][iou <= iou_thresh]

    return keep


def filter_vertebrae(vertebrae: list) -> list:
    if len(vertebrae) < 3:
        return [v for v in vertebrae if v["conf"] >= 0.4]

    heights = [v["h"] for v in vertebrae]
    widths  = [v["w"] for v in vertebrae]
    x_positions = [v["x"] for v in vertebrae]

    avg_h    = sum(heights) / len(heights)
    avg_w    = sum(widths)  / len(widths)
    median_x = sorted(x_positions)[len(x_positions) // 2]

    filtered = []
    for v in vertebrae:
        if v["h"] > 3 * avg_h or v["w"] > 3 * avg_w:
            continue
        if v["conf"] < 0.4:
            continue

        if abs(v["x"] - median_x) > avg_w * 3:
            continue
        filtered.append(v)

    return filtered


def detect_vertebrae(img: Image.Image):
    """
    Rulează YOLOv8 + NMS.
    Returnează lista de boxuri în coordonate originale:
      [{"x": cx, "y": cy, "w": w, "h": h, "conf": score}, ...]
    sortate de sus în jos (y crescător).
    """
    tensor, meta = preprocess_image(img)
    raw = session.run(None, {"images": tensor})[0][0]  # [5, 8400]

    xc, yc, w, h, conf = raw[0], raw[1], raw[2], raw[3], raw[4]
    mask = conf >= CONF_THRESH

    if mask.sum() == 0:
        return []

    boxes  = np.stack([xc[mask], yc[mask], w[mask], h[mask]], axis=1)
    scores = conf[mask]
    keep   = nms(boxes, scores, IOU_THRESH)

    scale = meta["scale"]
    pad_x = meta["pad_x"]
    pad_y = meta["pad_y"]

    results = []
    for idx in keep:
        cx_orig = (boxes[idx, 0] - pad_x) / scale
        cy_orig = (boxes[idx, 1] - pad_y) / scale
        w_orig  = boxes[idx, 2] / scale
        h_orig  = boxes[idx, 3] / scale
        results.append({
            "x":    round(float(cx_orig), 1),
            "y":    round(float(cy_orig), 1),
            "w":    round(float(w_orig),  1),
            "h":    round(float(h_orig),  1),
            "conf": round(float(scores[idx]), 3)
        })

    results.sort(key=lambda b: b["y"])
    results = filter_vertebrae(results)
    return results


def compute_cobb_angle(vertebrae: list) -> dict:
    """
    Algoritmul Cobb din centrele bounding box-urilor vertebrelor.

    Pas 1: Calculează panta segmentului dintre fiecare pereche
            de vertebre consecutive (unghi față de orizontală).
    Pas 2: Cobb angle = unghiul maxim dintre oricare două segmente
            inter-vertebrale.
    """
    if len(vertebrae) < 3:
        return {"angle": None, "topIndex": None, "bottomIndex": None,
                "error": f"Prea puține vertebre detectate ({len(vertebrae)}). Minim 3."}

    centers = np.array([[v["x"], v["y"]] for v in vertebrae])
    n = len(centers)

    segment_angles = []
    for i in range(n - 1):
        dx = centers[i+1, 0] - centers[i, 0]
        dy = centers[i+1, 1] - centers[i, 1]
        angle_deg = math.degrees(math.atan2(dy, dx))
        segment_angles.append(angle_deg)

    # Cobb angle = diferenta maxima dintre oricare doua segmente
    max_diff    = 0.0
    top_idx     = 0
    bottom_idx  = n - 1

    for i in range(len(segment_angles)):
        for j in range(i + 1, len(segment_angles)):
            diff = abs(segment_angles[i] - segment_angles[j])
            if diff > 180:
                diff = 360 - diff
            if diff > max_diff:
                max_diff   = diff
                top_idx    = i
                bottom_idx = j + 1

    return {
        "angle":       round(max_diff, 1),
        "topIndex":    top_idx,
        "bottomIndex": bottom_idx,
        "segmentAngles": [round(a, 2) for a in segment_angles]
    }


def draw_visualization(img: Image.Image, vertebrae: list, cobb: dict) -> str:
    """
    Desenează pe imagine:
      - bounding boxes vertebre (galben)
      - linie coloană vertebrală (verde)
      - liniile Cobb (roșu) + unghiul
    Returnează PNG base64.
    """
    vis = img.convert("RGB").copy()
    draw = ImageDraw.Draw(vis)

    # Scala pentru grosimea liniilor (adaptiva la rezolutie)
    lw = max(2, min(vis.width, vis.height) // 200)

    centers = [(v["x"], v["y"]) for v in vertebrae]

    # 1. Bounding boxes
    for v in vertebrae:
        x1 = v["x"] - v["w"] / 2
        y1 = v["y"] - v["h"] / 2
        x2 = v["x"] + v["w"] / 2
        y2 = v["y"] + v["h"] / 2
        draw.rectangle([x1, y1, x2, y2], outline="#FFD700", width=lw)

    # 2. Linie coloana (conectează centrele)
    if len(centers) >= 2:
        draw.line(centers, fill="#00FF88", width=lw)
        for cx, cy in centers:
            r = lw * 2
            draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill="#00FF88")

    # 3. Liniile Cobb daca unghiul a fost calculat
    if cobb.get("angle") is not None and len(centers) >= 2:
        top_i    = cobb["topIndex"]
        bot_i    = cobb["bottomIndex"]
        line_len = max(vis.width, vis.height) * 0.15

        def draw_cobb_line(idx_a, idx_b, color):
            if idx_a >= len(centers) or idx_b >= len(centers):
                return
            cx_a, cy_a = centers[idx_a]
            cx_b, cy_b = centers[idx_b]
            dx = cx_b - cx_a
            dy = cy_b - cy_a
            length = math.hypot(dx, dy) or 1
            ux, uy = dx / length, dy / length
            x1_ext = cx_a - ux * line_len
            y1_ext = cy_a - uy * line_len
            x2_ext = cx_a + ux * line_len
            y2_ext = cy_a + uy * line_len
            draw.line([(x1_ext, y1_ext), (x2_ext, y2_ext)],
                      fill=color, width=lw + 1)

        draw_cobb_line(top_i, top_i + 1 if top_i + 1 < len(centers) else top_i - 1, "#FF4444")
        draw_cobb_line(bot_i - 1 if bot_i > 0 else bot_i + 1, bot_i, "#FF4444")

        # Text unghi
        mid_x = (centers[top_i][0] + centers[bot_i][0]) / 2
        mid_y = (centers[top_i][1] + centers[bot_i][1]) / 2
        label = f"Cobb: {cobb['angle']}°"
        draw.text((mid_x + lw * 4, mid_y), label, fill="#FF4444")

    # Encode PNG → base64
    buf = io.BytesIO()
    vis.save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("utf-8")



@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok" if MODEL_OK else "model_not_loaded",
        "model":  "YOLOv8s Vertebrae Detector + Cobb Angle Calculator",
        "confThreshold": CONF_THRESH,
        "iouThreshold":  IOU_THRESH,
    })

@app.route("/predict-cobb", methods=["POST"])
def predict_cobb():
    if not MODEL_OK:
        return jsonify({"error": "Model not loaded"}), 503

    if "image" not in request.files:
        return jsonify({"error": "Missing 'image' field in multipart form"}), 400

    include_viz = request.form.get("visualization", "true").lower() == "true"

    try:
        file_bytes = request.files["image"].read()
        img        = Image.open(io.BytesIO(file_bytes)).convert("RGB")

        # 1. Detectie vertebre
        vertebrae  = detect_vertebrae(img)

        if len(vertebrae) == 0:
            return jsonify({
                "cobbAngle":      None,
                "severity":       None,
                "vertebraeCount": 0,
                "vertebrae":      [],
                "visualization":  None,
                "message":        "Nicio vertebră detectată. Verificați calitatea imaginii."
            })

        # 2. Calcul unghi Cobb
        cobb = compute_cobb_angle(vertebrae)

        # 3. Vizualizare optionala
        viz_b64 = None
        if include_viz:
            viz_b64 = draw_visualization(img, vertebrae, cobb)

        angle    = cobb.get("angle")
        severity = cobb_to_severity(angle) if angle is not None else None

        return jsonify({
            "cobbAngle":      angle,
            "severity":       severity,
            "vertebraeCount": len(vertebrae),
            "vertebrae":      vertebrae,
            "cobbDetails": {
                "topVertebra":    cobb.get("topIndex"),
                "bottomVertebra": cobb.get("bottomIndex"),
                "segmentAngles":  cobb.get("segmentAngles", []),
                "error":          cobb.get("error")
            },
            "visualization":  viz_b64   # PNG base64, null dacă visualization=false
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=False)