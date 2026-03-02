import pandas as pd
import cv2
import numpy as np
from tensorflow.keras.models import load_model
from math import atan, pi

# -------- LOAD MODEL --------
model = load_model(r"D:\Final Year Project\MachineLearning\Notebooks\skin_model.h5")

# -------- LOAD DDI METADATA --------
ddi = pd.read_csv(r"D:\Final Year Project\ImageProcessing\DDI\ddidiversedermatologyimages\ddi_metadata.csv")

results = []

def calculate_ita(image):
    image = cv2.resize(image, (224, 224))
    ita_values = []

    for y in range(0, image.shape[0], 5):
        for x in range(0, image.shape[1], 5):
            b, g, r = image[y, x]
            L = 0.2126*r + 0.7152*g + 0.0722*b
            B = b - g
            if B != 0:
                ita = atan((L - 50)/B) * 180/pi
                ita_values.append(ita)

    return np.median(ita_values) if ita_values else 0


for _, row in ddi.iterrows():

    filename = row["DDI_file"]
    ground_truth = 1 if row["malignant"] == True else 0

    image_path = r"D:\Final Year Project\ImageProcessing\DDI\ddidiversedermatologyimages\\" + filename
    image = cv2.imread(image_path)

    if image is None:
        continue

    # preprocess for model
    resized = cv2.resize(image, (224, 224)) / 255.0
    resized = np.expand_dims(resized, axis=0)

    prediction = model.predict(resized, verbose=0)[0][0]
    predicted_class = 1 if prediction > 0.5 else 0

    ita_value = calculate_ita(image)

    results.append({
        "filename": filename,
        "ground_truth": ground_truth,
        "prediction": predicted_class,
        "ita_value": ita_value
    })

df_results = pd.DataFrame(results)
df_results.to_csv("ddi_evaluation_results.csv", index=False)

print("Evaluation complete.")
