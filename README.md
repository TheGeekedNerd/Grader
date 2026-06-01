Grader is a native Android app designed to help students track their academic performance. It allows users to dynamically manage course modules and add individual assessments with custom categories, weights, and marks. The app provides a real-time calculation of the overall module grade, automatically averaging scores for recurring assessments like quizzes. All data is stored locally on the device using SharedPreferences and Google's Gson library for fast and efficient offline access. The app includes smart validation to prevent duplicate entries and maintain data integrity.

### Key Features:

*   **Dynamic Module Management:** Easily create, edit, and delete course modules.
*   **Flexible Assessment Tracking:**
    *   Add and remove assessments dynamically within each module.
    *   Assign assessments to predefined categories (e.g., Lab Test, Quiz) or create custom-named assessments with the "Other" option.
    *   Each assessment is automatically numbered for easy tracking.
*   **Powerful Grade Calculation:**
    *   Enter marks as fractions (e.g., `15/20`), and the app instantly calculates the percentage.
    *   Assign custom weights to each assessment to accurately reflect its contribution to the final grade.
    *   The app automatically averages assessments of the same type before applying their weight.
*   **Real-Time Feedback:** Your total module grade is calculated and updated in real-time as you modify assessment data.
*   **Smart Validation:**
    *   Prevents the creation of modules with a duplicate name or course code.
    *   Ensures that assessments within the same category have unique numbers.
    *   Requires both marks and weights to be filled before saving.
*   **Offline & Private:** All data is stored locally on your device and is never shared. The app works perfectly offline, and a fresh installation on a new device will always start clean.
*   **Consistent Formatting:** Course names and codes are automatically saved in uppercase for a uniform look.

### Motivation behind creating this app:
"I was just thinking about how I have been storing or keeping track of my marks and I realized that it was somewhat a tedious thing to do. Having to create a table on a piece of paper and create a spreadsheet where I write and add marks. If I happened to be lucky my marks or mark % contribution would change so I would start the whole process from scratch. Also the process of calculating my marks using a calculator was also a drag. So I was like why not just implement a mobile app to solve this. Reason why I chose Android Studio was because the project that I was a part of in the Mobile Computing course, Second Year Computer Science was bad, the UI horrible so I was like let me try doing something that looks nice using Android Studio. I've made vows to myself that I wouldn't touch Android Studio after this project but we'll see."
