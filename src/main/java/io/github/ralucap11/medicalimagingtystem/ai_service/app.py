"""
Scoliosis Severity Classifier - Flask Microservice
Model: ConvNeXt Tiny (fine-tuned)
Classes: Normal, Mild, Moderate, Severe
"""

from flask import Flask, request, jsonify
import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
from PIL import Image
import io
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)


MODEL_PATH = "best_scoliosis_v2.pth"
CLASSES = ["Normal", "Mild", "Moderate", "Severe"]
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


def build_model():
    model = models.convnext_tiny(weights=None)

    model.avgpool = nn.Sequential(
        nn.AdaptiveAvgPool2d(1),
        nn.Flatten()              # [1, 768, 1, 1] → [1, 768]
    )

    model.classifier = nn.Sequential(
        nn.LayerNorm((768,), eps=1e-6),
        nn.Flatten(),
        nn.Sequential(
            nn.Linear(768, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 4)
        )
    )
    return model


logger.info(f"Loading model from {MODEL_PATH} on {DEVICE}...")
model = build_model()
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model.to(DEVICE)
model.eval()
logger.info("Model loaded successfully.")


val_transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])


tta_transforms = [
    val_transform,
    transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.RandomHorizontalFlip(p=1.0),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ]),
    transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.functional.rotate if False else transforms.RandomRotation((5, 5)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ]),
]


def predict_single(image: Image.Image) -> dict:
    """Run inference on a single PIL image, returns class + probabilities."""
    tensor = val_transform(image).unsqueeze(0).to(DEVICE)

    with torch.no_grad():
        output = model(tensor)
        probs = torch.softmax(output, dim=1)[0]

    predicted_idx = probs.argmax().item()
    return {
        "predictedClass": CLASSES[predicted_idx],
        "predictedIndex": predicted_idx,
        "confidence": round(probs[predicted_idx].item() * 100, 2),
        "probabilities": {
            cls: round(probs[i].item() * 100, 2)
            for i, cls in enumerate(CLASSES)
        }
    }




@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "model": "ConvNeXt-Tiny Scoliosis Classifier"})


@app.route("/predict", methods=["POST"])
def predict():
    if "image" not in request.files:
        return jsonify({"error": "No image file provided. Use key 'image'."}), 400

    file = request.files["image"]
    if file.filename == "":
        return jsonify({"error": "Empty filename"}), 400

    try:
        img_bytes = file.read()
        image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    except Exception as e:
        return jsonify({"error": f"Could not read image: {str(e)}"}), 400

    result = predict_single(image)
    logger.info(f"Prediction: {result['predictedClass']} ({result['confidence']}%)")
    return jsonify(result)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=False)