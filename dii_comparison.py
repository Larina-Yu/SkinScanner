import pandas as pd
import matplotlib.pyplot as plt

# ---- PATHS ----
ddi_csv = r"D:\Final Year Project\ImageProcessing\DDI\ddidiversedermatologyimages\ddi_metadata.csv"
your_csv = r"D:\Final Year Project\ImageProcessing\ddi_skin_tone_results.csv"

# ---- Load ----
ddi_df = pd.read_csv(ddi_csv)
your_df = pd.read_csv(your_csv)

# ---- DDI distribution ----
ddi_dist = ddi_df["skin_tone"].value_counts().sort_index()

# ---- Convert your tones to numeric ----
tone_map = {
    "I": 12,
    "II": 12,
    "III": 34,
    "IV": 34,
    "V": 56,
    "VI": 56
}

your_df["tone_numeric"] = your_df["skin_tone"].map(tone_map)

your_dist = your_df["tone_numeric"].value_counts().sort_index()

print("DDI Distribution:")
print(ddi_dist)

print("\nYour Dataset Distribution:")
print(your_dist)

# ---- Plot Comparison ----
comparison = pd.DataFrame({
    "DDI": ddi_dist,
    "Yours": your_dist
}).fillna(0)

comparison.plot(kind="bar")
plt.title("Skin Tone Distribution Comparison")
plt.xlabel("Tone Group")
plt.ylabel("Count")
plt.show()
