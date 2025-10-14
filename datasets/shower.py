import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

def analyze_sensor_data_with_numeric_temporal(csv_file_path):
    """
    Reads sensor data from a CSV, extracts numeric temporal features (month, weekday, hour of day),
    transforms it to a wide format, and generates a pair plot
    to visualize field clustering including these temporal features.

    Args:
        csv_file_path (str): The path to the input CSV file.
    """
    if not os.path.exists(csv_file_path):
        print(f"Oops! The file '{csv_file_path}' doesn't seem to exist. "
              "Please double-check the path and try again.")
        return

    print(f"🚀 Starting enhanced data analysis for '{csv_file_path}' (with numeric temporal features)...")

    try:
        # 1. Read the CSV file into a pandas DataFrame
        df = pd.read_csv(csv_file_path, parse_dates=['_start', '_stop', '_time'])
        print("\n✅ CSV file loaded successfully!")
        print("Here's a peek at the raw data:")
        print(df.head())

        # Define the original field types we are interested in for pivoting
        field_types_for_pivot = ['temperature', 'volume', 'duration']
        df_filtered = df[df['_field'].isin(field_types_for_pivot)].copy()

        if df_filtered.empty:
            print("\n⚠️ No data found for 'temperature', 'volume', or 'duration' fields. "
                  "Please ensure your CSV contains these values in the '_field' column.")
            return

        # 2. Create new columns for numeric month, weekday, and hour of day
        print("\nExtracting numeric month, weekday, and hour of day from '_time'...")
        # .dt.month gives 1-12 for Jan-Dec
        df_filtered['month_num'] = df_filtered['_time'].dt.month
        # .dt.weekday gives 0-6 for Mon-Sun
        df_filtered['weekday_num'] = df_filtered['_time'].dt.weekday
        # .dt.hour gives 0-23
        df_filtered['hour_of_day'] = df_filtered['_time'].dt.hour

        print("Here's df_filtered with the new numeric temporal columns:")
        print(df_filtered[['_time', 'month_num', 'weekday_num', 'hour_of_day']].head())

        # 3. Create a wide-form DataFrame
        # We'll use '_time' and '_measurement' as unique identifiers for each set of readings
        # and pivot '_field' values into new columns using '_value'.
        wide_df = df_filtered.pivot_table(
            index=['_time', '_measurement'],
            columns='_field',
            values='_value'
        ).reset_index()

        wide_df.columns.name = None # Remove the 'columns' name from the index
        print("\n✅ Data successfully transformed to wide format!")
        print("Here's a peek at the wide-form data:")
        print(wide_df.head())

        # Now, derive the temporal columns for the wide_df from its _time column.
        # This ensures these temporal columns are correctly aligned with the pivoted data.
        wide_df['month_num'] = wide_df['_time'].dt.month
        wide_df['weekday_num'] = wide_df['_time'].dt.weekday
        wide_df['hour_of_day'] = wide_df['_time'].dt.hour

        # Ensure the original measurement columns are numeric
        for col in field_types_for_pivot:
            if col in wide_df.columns:
                wide_df[col] = pd.to_numeric(wide_df[col], errors='coerce')
            else:
                print(f"Warning: Column '{col}' not found in wide_df. It might be missing in your CSV for all entries.")

        # Drop rows where any of the key fields are NaN after pivoting
        wide_df.dropna(subset=field_types_for_pivot, inplace=True)

        if wide_df.empty:
            print("\n⚠️ After transformation and cleaning, no complete data rows remain for plotting.")
            return

        print("\nFinal wide-form data with all numeric features (including temporal):")
        print(wide_df.head())

        # Define all columns to be included in the pair plot
        # These are your original fields plus the new numeric temporal ones
        columns_for_plotting = field_types_for_pivot + ['month_num', 'weekday_num', 'hour_of_day']

        # 4. Create a graphical plot to show clustering of these fields
        print("\n📊 Generating the clustering plot including temporal features...")

        sns.set_style("whitegrid")
        plt.figure(figsize=(12, 10)) # Adjust figure size for more variables

        # Create the PairPlot with all specified numeric columns
        g = sns.pairplot(
            wide_df,
            vars=columns_for_plotting, # Now includes 'month_num', 'weekday_num', 'hour_of_day'
            diag_kind="kde",         # Show kernel density estimate on the diagonal
            height=2.5,              # Height of each facet
            aspect=1,                # Aspect ratio of each facet
            plot_kws={'alpha': 0.7, 's': 30} # Adjust transparency and size of scatter points
        )

        g.fig.suptitle('Clustering of Sensor Fields (Measurements & Numeric Temporal Features)', y=1.02) # Add a main title

        plt.tight_layout(rect=[0, 0, 1, 0.98]) # Adjust layout to prevent title overlap
        plt.show()

        print("\n🎉 Plot generated successfully! You should see a window with the visualization.")

    except Exception as e:
        print(f"\nAn error occurred during processing: {e}")
        print("Please ensure your CSV file is correctly formatted and contains the expected columns.")

# --- How to use the program ---
if __name__ == "__main__":
    # Create a dummy CSV file for demonstration purposes
    dummy_csv_content = """result,table,_dummy,_start,_stop,_time,_value,_field,_measurement
_result,0,0,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,25.5,temperature,main_sensor
_result,0,0,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,10.2,volume,main_sensor
_result,0,0,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,2023-01-01T10:00:00Z,60,duration,main_sensor
_result,0,0,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,26.1,temperature,main_sensor
_result,0,0,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,10.5,volume,main_sensor
_result,0,0,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,2023-01-01T10:01:00Z,62,duration,main_sensor
_result,0,0,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,28.0,temperature,main_sensor
_result,0,0,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,11.0,volume,main_sensor
_result,0,0,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,2023-01-02T11:00:00Z,65,duration,main_sensor
_result,0,0,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,25.0,temperature,main_sensor
_result,0,0,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,10.0,volume,main_sensor
_result,0,0,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,2023-01-03T14:30:00Z,58,duration,main_sensor
_result,0,0,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,30.0,temperature,main_sensor
_result,0,0,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,12.0,volume,main_sensor
_result,0,0,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,2023-01-04T09:00:00Z,70,duration,main_sensor
_result,0,0,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,27.0,temperature,main_sensor
_result,0,0,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,11.5,volume,main_sensor
_result,0,0,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,2023-01-05T20:00:00Z,68,duration,main_sensor
_result,0,0,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,22.0,temperature,main_sensor
_result,0,0,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,9.0,volume,main_sensor
_result,0,0,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,2023-02-10T08:00:00Z,55,duration,main_sensor
_result,0,0,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,29.0,temperature,main_sensor
_result,0,0,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,13.0,volume,main_sensor
_result,0,0,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,2023-03-15T13:00:00Z,75,duration,main_sensor
"""
    dummy_csv_filename = "sensor_data_numeric_temporal.csv" # New filename for this version
    with open(dummy_csv_filename, "w") as f:
        f.write(dummy_csv_content)
    print(f"Created '{dummy_csv_filename}' for demonstration.")

    # Call the function with your CSV file path
    #analyze_sensor_data_with_numeric_temporal(dummy_csv_filename)
    csv_filename = "D:\Personal\\teaching\distributed-systems\excercises\\assignment-3\github-repo\DS2025\BCS-DS-Assignments\Assignment3\BSC-DS-Assignment-Package\datasets\dataset-showering.csv"
    analyze_sensor_data_with_numeric_temporal(csv_filename)

    # Optional: You might want to clean up the dummy file after running
    # os.remove(csv_filename)
    # print(f"Removed '{csv_filename}'.")