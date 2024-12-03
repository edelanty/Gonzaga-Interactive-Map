# Gonzaga Interactive Map
### What the Project Does
The Gonzaga Interactive Map is a mobile application designed to help Gonzaga University students navigate the campus more effectively. It provides an interactive map that includes key features like favorite locations, retrieving location information, and filtering spots of interest. Users can mark favorite spots, apply filters for specific location types (e.g., food, study areas, or classrooms), and enable notifications for selected favorites.

### Why the Project is Useful
This app enhances the campus experience by offering students a dynamic, student-driven map where they can explore and provide feedback on various campus locations. It can be especially helpful for newcomers or anyone looking to quickly discover popular spots for studying, eating, socializing, and all that campus has to offer.

### How to Get Started
*Before continuing with these steps, for easiest navigation to your old Android Studio make sure you pin your current version of Android Studio to your taskbar*

The following instructions are to ensure Firebase works as way we got this working was through the Android Studio Assistant (Option 2 in this setup link): https://firebase.google.com/docs/android/setup#assistant 

##### **Instructions:**
**1. In your current version of Android Studio, navigate to the help tab in the hamburger menu on the top left. Then under the "Help" section at the top:**

* Click "Check for Updates..."
* In the bottom right there should be a pop-up that states "Android Studio Ladybug" 2024.2.1 Patch 2 Available. Click "Update"
* Another popup displays, click "Download"
* It should bring you to the webpage to download. If it doesn't here is the link: https://developer.android.com/studio

**2. From their website, click "Download Android Studio Ladybug":**

* Read and accept their terms
* Allow for it to download
* Click on the download from the downloads folder
* Close the running version of Android Studio
* Press "OK" to the popup to install (accept admin)

**3. Once in the setup menu after clicking downloaded item:**

* On the first page it is important to uncheck "Uninstall previous version from setup" **THIS ENSURES YOUR PREVIOUS VERSION OF ANDROID STUDIO AND YOUR FILES ARE NOT DELETED**
* In the setup once unchecked, click "next" 5 times until installation occurs
* Startup Android Studio Ladybug (Also pin this to your taskbar and rename to distinguish it from your previous version of Android Studio)
* Wait for everything to configure (Should take around 3 minutes)
* Once everything completes, click "Start AGP Upgrade Assistant" from the bottom right pop-up 
* Make sure the Gradle Plugin version is set to 8.7.2 and click "Run selected steps" (blue button)
* Allow for "Running Sync" to finish (Should take around 5 minutes)
* If you see a popup after this that displays upgrade for emulator feel free to press remind me later

**4. Clone the repository:**
* git clone https://github.com/GU-APP-DEV-2024/mobile-app-dev-project-deliverable-2-aee.git

**5. Set up API key:**
* If you do not already have a Google Maps API Key then follow these instructions from Google: https://developers.google.com/maps/documentation/javascript/get-api-key
* When you have a key, paste your Google Maps API Key in local.properties. Format is: MAPS_API_KEY=**INSERT API KEY HERE**

**6. Run the app on your emulator:**
* Ensure you have an emulator set up
* Build and run the app to see the interactive map of Gonzaga University.

### Where to Get Help
If you encounter any issues with the project, please contact us through the project's GitHub issues page.

### Maintainers and Contributors
* AJ Cononetz - ajcononetz3
* Evan Delanty - edelanty
* Ethan Danitz - edanitz02