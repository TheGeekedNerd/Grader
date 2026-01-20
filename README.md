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
