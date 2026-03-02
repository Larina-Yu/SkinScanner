import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load results from the CSV
df = pd.read_csv("ddi_evaluation_results.csv")

# Add a new column to track failures
df['failure'] = df['prediction'] != df['ground_truth']

# Filter the failures
failures = df[df['failure']]

# Plot distribution of ITA values for failed predictions
plt.figure(figsize=(10, 6))
sns.histplot(failures['ita_value'], kde=True, bins=15, color='red')
plt.title("Distribution of ITA values for Classification Failures")
plt.xlabel("ITA Value")
plt.ylabel("Frequency")
plt.show()

# Plot ITA values vs prediction failures
plt.figure(figsize=(10, 6))
sns.boxplot(x='failure', y='ita_value', data=df, palette="Set2")
plt.title("ITA Value vs Prediction Failure")
plt.xlabel("Prediction Failure (True/False)")
plt.ylabel("ITA Value")
plt.show()


