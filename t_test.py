import pandas as pd
from scipy.stats import ttest_ind

# Load evaluation results
df = pd.read_csv("ddi_evaluation_results.csv")

# Create failure column
df['failure'] = df['prediction'] != df['ground_truth']

# Split ITA values
fail_ita = df[df['failure']]['ita_value']
success_ita = df[~df['failure']]['ita_value']

# Perform independent t-test
t_stat, p_value = ttest_ind(fail_ita, success_ita, equal_var=False)

print("Number of failures:", len(fail_ita))
print("Number of successes:", len(success_ita))
print("T-statistic:", t_stat)
print("P-value:", p_value)

if p_value < 0.05:
    print("Statistically significant difference in ITA between failures and successes.")
else:
    print("No statistically significant difference in ITA.")
