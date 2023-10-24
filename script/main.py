import pandas as pd

# Read the CSV file into a DataFrame
df = pd.read_csv('dataset.csv')

# Filter rows where 'activity_name' is not equal to 'Idle'
filtered_df = df[df['activity_name'] != 'Idle']

# Save the filtered DataFrame to a new CSV file
filtered_df.to_csv('dataset_preprocess.csv', index=False)

print(f"Filtered dataset saved to 'dataset_preprocess.csv'")
