# Teamwork App on Android

This app aims at giving all **Geekskool** students a way to assign tasks to each other and communicate on the progress of those tasks. The project uses [Firebase](https://www.firebase.com/) as a backend and needs an active internet connection to function.

### Installation
Download the apk [here](https://drive.google.com/file/d/0B9I0dOuwMRm2MEVDb0dhemtVbjA/view?usp=docslist_api). Check "Unknown Sources" in your phone's security settings, and install the apk. If you have **Android Studio** installed on your computer, you can also `clone` this repository and run the project on your phone.

After installation, you would need to provide your phone number to sign up since only **Geekskool** students are authorized to use this app. You would receive an OTP for authentication. You can start assigning tasks after successful authentication.

### Functionality
**Add Task:** Can be found on swiping left on the launching screen (after login) or by tapping on the `floating action button`. Allows a student/mentor to assign a task to a **Geekskool** member. Tasks cannot be assigned to non-members.

**Existing Tasks:** Allows people to see the tasks that they created or were assigned. If there are no tasks assigned to or created by them, a pop up dialog notifies the user of that.

**Comments:** When one of the tasks is selected, the user is taken to the "Comments" screen where they can chat with the creator/assignee on the progress of the task.

**Edit Profile:** This can be found by tapping on the three dot `menu` button on the top right corner of the `action bar` in the main screen. This allows a user to upload their profile photo and edit their name on the database.

### Understanding the Code
Each class has its own documentation as `comments` in the `.java` files.

### Known Bugs
* Notifications do not always appear as soon as a comment or a new task is posted on Firebase.
* On Marshmallow devices, the datepicker dialog appears blank.
* OTP does not get delivered to some phone numbers.
* Swiping right from **Existing Tasks** to the **Comments** `fragment` does not always show the comments.

### Next Steps
* Fix the bugs.
* Implement a functionality to create teams and for team members to be able to see and comment on each other's tasks.
* Show the picture of the commenter on each comment.
