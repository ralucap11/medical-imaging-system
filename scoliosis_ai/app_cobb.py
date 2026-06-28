

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
CONF_THRESH = 0.10
IOU_THRESH  = 0.45


def cobb_to_severity(angle: float) -> str:
    if angle < 10:  return "Normal"
    if angle < 25:  return "Mild"
    if angle < 45:  return "Moderate"
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
    scale  = min(INPUT_SIZE / orig_w, INPUT_SIZE / orig_h)
    new_w  = int(orig_w * scale)
    new_h  = int(orig_h * scale)
    resized = img.resize((new_w, new_h), Image.BILINEAR)

    canvas = Image.new("RGB", (INPUT_SIZE, INPUT_SIZE), (114, 114, 114))
    pad_x  = (INPUT_SIZE - new_w) // 2
    pad_y  = (INPUT_SIZE - new_h) // 2
    canvas.paste(resized, (pad_x, pad_y))

    arr  = np.array(canvas, dtype=np.float32) / 255.0
    arr  = arr.transpose(2, 0, 1)[np.newaxis]
    meta = {"scale": scale, "pad_x": pad_x, "pad_y": pad_y,
            "orig_w": orig_w, "orig_h": orig_h}
    return arr, meta




def nms(boxes_xywh, scores, iou_thresh):
    if len(boxes_xywh) == 0:
        return []

    x1 = boxes_xywh[:, 0] - boxes_xywh[:, 2] / 2
    y1 = boxes_xywh[:, 1] - boxes_xywh[:, 3] / 2
    x2 = boxes_xywh[:, 0] + boxes_xywh[:, 2] / 2
    y2 = boxes_xywh[:, 1] + boxes_xywh[:, 3] / 2
    areas  = (x2 - x1) * (y2 - y1)
    order  = scores.argsort()[::-1]
    keep   = []

    while len(order):
        i = order[0]
        keep.append(i)
        xx1   = np.maximum(x1[i], x1[order[1:]])
        yy1   = np.maximum(y1[i], y1[order[1:]])
        xx2   = np.minimum(x2[i], x2[order[1:]])
        yy2   = np.minimum(y2[i], y2[order[1:]])
        inter = np.maximum(0, xx2 - xx1) * np.maximum(0, yy2 - yy1)
        iou   = inter / (areas[i] + areas[order[1:]] - inter + 1e-9)
        order = order[1:][iou <= iou_thresh]

    return keep


def filter_vertebrae(vertebrae: list) -> list:
    if len(vertebrae) == 0:
        return []

    heights = [v["h"] for v in vertebrae]
    widths  = [v["w"] for v in vertebrae]
    avg_h   = sum(heights) / len(heights)
    avg_w   = sum(widths)  / len(widths)


    vertebrae = [v for v in vertebrae
                 if v["h"] <= 3 * avg_h and v["w"] <= 3 * avg_w
                 and v["conf"] >= 0.10]

    if len(vertebrae) < 3:
        return vertebrae


    x_vals = sorted(v["x"] for v in vertebrae)
    n      = len(x_vals)
    q1     = x_vals[n // 4]
    q3     = x_vals[3 * n // 4]
    iqr    = max(q3 - q1, avg_w)
    x_lo   = q1 - 1.5 * iqr
    x_hi   = q3 + 1.5 * iqr

    return [v for v in vertebrae if x_lo <= v["x"] <= x_hi]

def detect_vertebrae(img: Image.Image):
    tensor, meta = preprocess_image(img)
    raw  = session.run(None, {"images": tensor})[0][0]   # [5, 8400]

    xc, yc, w, h, conf = raw[0], raw[1], raw[2], raw[3], raw[4]
    mask = conf >= CONF_THRESH

    if mask.sum() == 0:
        return []

    boxes  = np.stack([xc[mask], yc[mask], w[mask], h[mask]], axis=1)
    scores = conf[mask]
    keep   = nms(boxes, scores, IOU_THRESH)

    scale, pad_x, pad_y = meta["scale"], meta["pad_x"], meta["pad_y"]

    results = []
    for idx in keep:
        cx_orig = (boxes[idx, 0] - pad_x) / scale
        cy_orig = (boxes[idx, 1] - pad_y) / scale
        w_orig  =  boxes[idx, 2] / scale
        h_orig  =  boxes[idx, 3] / scale
        results.append({
            "x":    round(float(cx_orig), 1),
            "y":    round(float(cy_orig), 1),
            "w":    round(float(w_orig),  1),
            "h":    round(float(h_orig),  1),
            "conf": round(float(scores[idx]), 3)
        })

    results.sort(key=lambda b: b["y"])

    print(f"[DEBUG] Raw detections before filter: {len(results)}")
    for r in results:
        print(f"  conf={r['conf']:.3f}  x={r['x']}  y={r['y']}")

    results = filter_vertebrae(results)
    return results




def smooth_centers(vertebrae: list, window: int = 3) -> list:

    if len(vertebrae) < window:
        return vertebrae

    centers_x = np.array([v["x"] for v in vertebrae], dtype=float)
    kernel    = np.ones(window) / window
    smoothed  = np.convolve(centers_x, kernel, mode="same")


    half             = window // 2
    smoothed[:half]  = centers_x[:half]
    smoothed[-half:] = centers_x[-half:]

    result = []
    for i, v in enumerate(vertebrae):
        new_v      = v.copy()
        new_v["x"] = round(float(smoothed[i]), 1)
        result.append(new_v)
    return result


def compute_lateral_deviations(centers: np.ndarray) -> np.ndarray:


    p1       = centers[0]
    p2       = centers[-1]
    baseline = p2 - p1
    bl_len   = np.linalg.norm(baseline)

    if bl_len < 1e-6:
        return np.zeros(len(centers))

    deviations = []
    for pt in centers:
        t    = np.dot(pt - p1, baseline) / (bl_len ** 2)
        proj = p1 + t * baseline
        deviations.append(float(pt[0] - proj[0]))

    return np.array(deviations)


def find_inflection_index(deviations: np.ndarray) -> int:


    signs = np.sign(deviations)
    for i in range(1, len(signs) - 1):
        prev, nxt = signs[i - 1], signs[i + 1]
        if prev != 0 and nxt != 0 and prev != nxt:
            return i
    return -1


def _cobb_for_segment(vertebrae_subset: list) -> dict:


    if len(vertebrae_subset) < 3:
        return {
            "angle": None,
            "topIndex": None, "bottomIndex": None, "apexIndex": None,
            "segmentAngles": [],
            "error": f"Segment prea scurt ({len(vertebrae_subset)} vertebre). Minim 3."
        }

    centers = np.array([[v["x"], v["y"]] for v in vertebrae_subset])
    n       = len(centers)


    segment_angles = []
    for i in range(n - 1):
        dx = centers[i + 1, 0] - centers[i, 0]
        dy = centers[i + 1, 1] - centers[i, 1]
        segment_angles.append(math.degrees(math.atan2(dy, dx)))


    deviations = compute_lateral_deviations(centers)
    apex_idx   = int(np.argmax(np.abs(deviations)))
    apex_idx   = max(1, min(apex_idx, n - 2))


    top_range = segment_angles[:apex_idx]
    top_idx   = int(np.argmax(np.abs(top_range))) if top_range else 0


    bot_range = segment_angles[apex_idx:]
    bot_idx   = apex_idx + int(np.argmax(np.abs(bot_range))) if bot_range else n - 2

    angle_diff = abs(segment_angles[top_idx] - segment_angles[bot_idx])
    if angle_diff > 180:
        angle_diff = 360 - angle_diff

    return {
        "angle":         round(angle_diff, 1),
        "topIndex":      top_idx,
        "bottomIndex":   bot_idx + 1,
        "apexIndex":     apex_idx,
        "segmentAngles": [round(a, 2) for a in segment_angles],
        "error":         None
    }


def compute_cobb_angle(vertebrae: list) -> dict:


    if len(vertebrae) < 3:
        return {
            "angle": None, "topIndex": None, "bottomIndex": None,
            "apexIndex": None, "isDoubleCurve": False,
            "primaryCurve": None, "secondaryCurve": None,
            "segmentAngles": [],
            "error": f"Prea puține vertebre detectate ({len(vertebrae)}). Minim 3."
        }

    smoothed   = smooth_centers(vertebrae, window=3)
    centers    = np.array([[v["x"], v["y"]] for v in smoothed])
    deviations = compute_lateral_deviations(centers)

    inflection = find_inflection_index(deviations)



    is_double = (
            inflection != -1
            and inflection >= 3
            and inflection <= len(vertebrae) - 3
    )

    if not is_double:

        result = _cobb_for_segment(smoothed)
        return {
            **result,
            "isDoubleCurve":  False,
            "primaryCurve":   None,
            "secondaryCurve": None,
        }




    upper_vertebrae = smoothed[:inflection + 1]
    lower_vertebrae = smoothed[inflection:]

    curve1 = _cobb_for_segment(upper_vertebrae)
    curve2 = _cobb_for_segment(lower_vertebrae)



    def to_absolute(curve_result: dict, offset: int) -> dict:
        r = curve_result.copy()
        for key in ("topIndex", "bottomIndex", "apexIndex"):
            if r.get(key) is not None:
                r[key] += offset
        return r

    curve1_abs = to_absolute(curve1, 0)
    curve2_abs = to_absolute(curve2, inflection)


    angle1 = curve1.get("angle") or 0.0
    angle2 = curve2.get("angle") or 0.0

    if angle1 >= angle2:
        primary, secondary = curve1_abs, curve2_abs
    else:
        primary, secondary = curve2_abs, curve1_abs

    return {
        "angle":          primary["angle"],
        "topIndex":       primary["topIndex"],
        "bottomIndex":    primary["bottomIndex"],
        "apexIndex":      primary["apexIndex"],
        "segmentAngles":  primary["segmentAngles"],
        "isDoubleCurve":  True,
        "primaryCurve":   primary,
        "secondaryCurve": secondary,
        "error":          None,
    }




def draw_visualization(img: Image.Image, vertebrae: list, cobb: dict) -> str:


    vis  = img.convert("RGB").copy()
    draw = ImageDraw.Draw(vis)
    lw   = max(2, min(vis.width, vis.height) // 200)

    centers = [(v["x"], v["y"]) for v in vertebrae]


    for v in vertebrae:
        x1 = v["x"] - v["w"] / 2
        y1 = v["y"] - v["h"] / 2
        x2 = v["x"] + v["w"] / 2
        y2 = v["y"] + v["h"] / 2
        draw.rectangle([x1, y1, x2, y2], outline="#FFD700", width=lw)


    if len(centers) >= 2:
        draw.line(centers, fill="#00FF88", width=lw)
        for cx, cy in centers:
            r = lw * 2
            draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill="#00FF88")


    apex_idx = cobb.get("apexIndex")
    if apex_idx is not None and 0 <= apex_idx < len(centers):
        cx, cy = centers[apex_idx]
        r = lw * 4
        draw.ellipse([cx - r, cy - r, cx + r, cy + r],
                     outline="#FF8C00", width=lw + 1)


    def draw_cobb_line(idx_a: int, idx_b: int, color: str):
        if not (0 <= idx_a < len(centers) and 0 <= idx_b < len(centers)):
            return
        cx_a, cy_a = centers[idx_a]
        cx_b, cy_b = centers[idx_b]
        dx     = cx_b - cx_a
        dy     = cy_b - cy_a
        length = math.hypot(dx, dy) or 1
        ux, uy = dx / length, dy / length
        ext    = max(vis.width, vis.height) * 0.15
        draw.line(
            [(cx_a - ux * ext, cy_a - uy * ext),
             (cx_a + ux * ext, cy_a + uy * ext)],
            fill=color, width=lw + 1
        )


    if cobb.get("angle") is not None and len(centers) >= 2:
        top_i  = cobb.get("topIndex", 0)
        bot_i  = cobb.get("bottomIndex", len(centers) - 1)
        top_i  = max(0, min(top_i,  len(centers) - 1))
        bot_i  = max(0, min(bot_i,  len(centers) - 1))

        top_next = top_i + 1 if top_i + 1 < len(centers) else top_i - 1
        bot_prev = bot_i - 1 if bot_i > 0              else bot_i + 1

        draw_cobb_line(top_i, top_next, "#FF4444")
        draw_cobb_line(bot_prev, bot_i, "#FF4444")

        mid_x = (centers[top_i][0] + centers[bot_i][0]) / 2
        mid_y = (centers[top_i][1] + centers[bot_i][1]) / 2
        draw.text((mid_x + lw * 4, mid_y),
                  f"Cobb: {cobb['angle']}°", fill="#FF4444")


    if cobb.get("isDoubleCurve") and cobb.get("secondaryCurve"):
        sec   = cobb["secondaryCurve"]
        s_top = sec.get("topIndex", 0)
        s_bot = sec.get("bottomIndex", len(centers) - 1)
        s_top = max(0, min(s_top, len(centers) - 1))
        s_bot = max(0, min(s_bot, len(centers) - 1))

        st_next = s_top + 1 if s_top + 1 < len(centers) else s_top - 1
        sb_prev = s_bot - 1 if s_bot > 0               else s_bot + 1

        draw_cobb_line(s_top, st_next, "#4488FF")
        draw_cobb_line(sb_prev, s_bot, "#4488FF")

        mid_x2 = (centers[s_top][0] + centers[s_bot][0]) / 2
        mid_y2 = (centers[s_top][1] + centers[s_bot][1]) / 2 + 20
        draw.text((mid_x2 + lw * 4, mid_y2),
                  f"Cobb₂: {sec['angle']}°", fill="#4488FF")


    buf = io.BytesIO()
    vis.save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("utf-8")




@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status":        "ok" if MODEL_OK else "model_not_loaded",
        "model":         "YOLOv8s Vertebrae Detector + Cobb Angle Calculator v2",
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


        vertebrae = detect_vertebrae(img)

        if len(vertebrae) == 0:
            return jsonify({
                "cobbAngle":      None,
                "severity":       None,
                "vertebraeCount": 0,
                "vertebrae":      [],
                "visualization":  None,
                "message":        "Nicio vertebră detectată. Verificați calitatea imaginii."
            })


        viz_b64 = None
        if include_viz:
            viz_b64 = draw_visualization(img, vertebrae, {})


        max_conf  = max(v["conf"] for v in vertebrae)
        high_conf = [v for v in vertebrae if v["conf"] >= 0.25]

        is_reliable = (max_conf >= 0.40 and len(vertebrae) >= 4) \
                      or len(high_conf) >= 3

        if not is_reliable:
            return jsonify({
                "cobbAngle":      None,
                "severity":       None,
                "vertebraeCount": len(vertebrae),
                "vertebrae":      vertebrae,
                "visualization":  viz_b64,
                "message":        "Detectii insuficiente cu confidenta ridicata. Rezultat nesigur."
            })


        cobb = compute_cobb_angle(vertebrae)


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
                "apexVertebra":   cobb.get("apexIndex"),
                "segmentAngles":  cobb.get("segmentAngles", []),
                "isDoubleCurve":  cobb.get("isDoubleCurve", False),
                "primaryCurve":   cobb.get("primaryCurve"),
                "secondaryCurve": cobb.get("secondaryCurve"),
                "error":          cobb.get("error")
            },
            "visualization": viz_b64
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=False)