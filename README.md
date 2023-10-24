# sensors-data-collection
Using accelerometer and gyroscope sensors, performing data collection and storing locally in a .csv file
Build this project using Android Studio.

## Methodology
A timer starts to collect values for three activities[sit, walk, climb] with a prompt appearing on the screen to perform that activity.
This activity will run for five iterations, and within time span of 15 seconds per activity, the data will be collected.
Between 2 activities, 5-second of ideal data will also be collected, which can be further removed with the python3 script exists within the repository.
[NOTE: install pandas to run the script]
