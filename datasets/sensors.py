import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

def analyze_environmental_sensor_data(csv_file_path):
    """
    Reads environmental sensor data from a CSV, extracts numeric temporal features
    (month, weekday, hour of day), transforms it to a wide format,
    and generates a pair plot to visualize clustering of these fields.

    Args:
        csv_file_path (str): The path to the input CSV file.
    """
    if not os.path.exists(csv_file_path):
        print(f"Oops! The file '{csv_file_path}' doesn't seem to exist. "
              "Please double-check the path and try again.")
        return

    print(f"🚀 Starting analysis for environmental sensor data from '{csv_file_path}'...")

    try:
        # 1. Read the CSV file into a pandas DataFrame
        df = pd.read_csv(csv_file_path, parse_dates=['_start', '_stop', '_time'])
        print("\n✅ CSV file loaded successfully!")
        print("Here's a peek at the raw data:")
        print(df.head())

        # Define the environmental field types we are interested in for pivoting
        environmental_fields = ['temperature', 'humidity', 'co2', 'light', 'voc', 'motion']
        df_filtered = df[df['_field'].isin(environmental_fields)].copy()

        if df_filtered.empty:
            print(f"\n⚠️ No data found for any of the specified environmental fields: {', '.join(environmental_fields)}. "
                  "Please ensure your CSV contains these values in the '_field' column.")
            return

        # 2. Create new columns for numeric month, weekday, and hour of day
        print("\nExtracting numeric month, weekday, and hour of day from '_time'...")
        df_filtered['month_num'] = df_filtered['_time'].dt.month     # 1-12 for Jan-Dec
        df_filtered['weekday_num'] = df_filtered['_time'].dt.weekday # 0-6 for Mon-Sun
        df_filtered['hour_of_day'] = df_filtered['_time'].dt.hour   # 0-23

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
        for col in environmental_fields:
            if col in wide_df.columns:
                wide_df[col] = pd.to_numeric(wide_df[col], errors='coerce')
            else:
                print(f"Warning: Column '{col}' not found in wide_df. It might be missing in your CSV for some entries.")

        # Drop rows where any of the key fields are NaN after pivoting
        # This ensures we only plot complete sets of environmental readings
        wide_df.dropna(subset=environmental_fields, inplace=True)

        if wide_df.empty:
            print("\n⚠️ After transformation and cleaning, no complete data rows remain for plotting.")
            return

        print("\nFinal wide-form data with all numeric features (environmental and temporal):")
        print(wide_df.head())

        # Define all columns to be included in the pair plot
        # These are your environmental fields plus the new numeric temporal ones
        columns_for_plotting = environmental_fields + ['month_num', 'weekday_num', 'hour_of_day']

        # 4. Create a graphical plot to show clustering of these fields
        print("\n📊 Generating the clustering plot including environmental and temporal features...")

        sns.set_style("whitegrid")
        # Adjust figure size for the increased number of variables
        # (6 environmental + 3 temporal = 9 variables)
        plt.figure(figsize=(15, 12))

        # Create the PairPlot with all specified numeric columns
        g = sns.pairplot(
            wide_df,
            vars=columns_for_plotting, # Now includes all environmental and temporal features
            diag_kind="kde",         # Show kernel density estimate on the diagonal
            height=2,                # Height of each facet (smaller due to more plots)
            aspect=1,                # Aspect ratio of each facet
            plot_kws={'alpha': 0.7, 's': 20} # Adjust transparency and size of scatter points
        )

        g.fig.suptitle('Clustering of Environmental Sensor Data (Measurements & Numeric Temporal Features)', y=1.02) # Add a main title

        plt.tight_layout(rect=[0, 0, 1, 0.98]) # Adjust layout to prevent title overlap
        plt.show()

        print("\n🎉 Plot generated successfully! You should see a window with the visualization.")

    except Exception as e:
        print(f"\nAn error occurred during processing: {e}")
        print("Please ensure your CSV file is correctly formatted and contains the expected columns.")

# --- How to use the program ---
if __name__ == "__main__":
    # Create a dummy CSV file for demonstration purposes
    # This dummy data now includes temperature, humidity, co2, light, voc, and motion
    # with varied times across different days and months.
    dummy_csv_content = """result,table,_dummy,_start,_stop,_time,_value,_field,_measurement
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,22.5,temperature,room_A_sensor
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,45.1,humidity,room_A_sensor
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,450,co2,room_A_sensor
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,150,light,room_A_sensor
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,25,voc,room_A_sensor
_result,0,0,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,2023-01-01T08:00:00Z,0,motion,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,24.0,temperature,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,48.0,humidity,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,600,co2,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,400,light,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,50,voc,room_A_sensor
_result,0,0,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,2023-01-01T12:00:00Z,1,motion,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,23.0,temperature,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,46.5,humidity,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,500,co2,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,200,light,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,30,voc,room_A_sensor
_result,0,0,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,2023-01-02T09:00:00Z,0,motion,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,20.0,temperature,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,55.0,humidity,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,700,co2,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,300,light,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,80,voc,room_A_sensor
_result,0,0,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,2023-02-15T14:00:00Z,1,motion,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,26.0,temperature,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,60.0,humidity,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,900,co2,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,100,light,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,120,voc,room_A_sensor
_result,0,0,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,2023-03-20T18:00:00Z,0,motion,room_A_sensor
"""
    csv_filename = "environmental_sensor_data.csv" # New filename for this version
    with open(csv_filename, "w") as f:
        f.write(dummy_csv_content)
    print(f"Created '{csv_filename}' for demonstration.")

    # Call the function with your CSV file path
    #analyze_environmental_sensor_data(csv_filename)

    csv_filename = "D:\Personal\\teaching\distributed-systems\excercises\\assignment-3\github-repo\DS2025\BCS-DS-Assignments\Assignment3\BSC-DS-Assignment-Package\datasets\dataset-room-sensors.csv"
    analyze_environmental_sensor_data(csv_filename)

    # Optional: You might want to clean up the dummy file after running
    # os.remove(csv_filename)
    # print(f"Removed '{csv_filename}'.")