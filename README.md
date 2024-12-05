# Chatify - Real-Time Messaging Application

**Chatify** is a modern, feature-rich messaging application designed to enable seamless communication between users. It incorporates real-time messaging, location sharing, video messaging, and a user-friendly interface. The app is built using **Firebase Realtime Database**, **Firebase Storage**, and **Android Studio**, and integrates Google APIs for enhanced functionality.

---

## Features

### 1. **User Authentication**
- Secure sign-up and sign-in using Firebase Authentication.
- Users can log in with an email and password to access personalized chatrooms.

### 2. **Real-Time Messaging**
- Users can send and receive instant text messages.
- Messages are synchronized across devices in real-time using Firebase Realtime Database.

### 3. **Location Sharing**
- Share your current location with friends using Google Maps integration.
- The recipient receives a clickable Google Maps link for easy navigation.

### 4. **Video Messaging**
- Record videos directly using your device’s camera and microphone.
- Videos are uploaded to Firebase Storage and shared as clickable messages in the chat.

### 5. **User-Friendly Interface**
- Intuitive UI with smooth navigation and clean layouts.
- Chats are displayed with sender and receiver differentiation for better readability.

---

## Technical Details

### Architecture
- **Frontend**: Built in **Android Studio** with Java.
- **Backend**: Firebase Realtime Database for storing messages and Firebase Storage for videos.
- **API Integration**: Google Maps API for location sharing and media APIs for video recording.

### Tools and Libraries Used
- **Firebase Realtime Database**: For real-time synchronization of messages.
- **Firebase Storage**: For secure storage and retrieval of video files.
- **Google Maps API**: For generating and sharing location links.
- **Glide**: For efficient loading and displaying of video thumbnails.

---

## Installation and Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/username/chatify.git
   cd chatify
   ```
2. **Set Up Firebase**
  - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).**
  - Enable Authentication**, **Realtime Database**, and **Storage**.
  - Download the `google-services.json` file** and place it in the `app/` directory of your project.
3. **Enable Google Maps API**
- Enable the Google Maps API for your project in the Google Cloud Console.
- Add the API key to the AndroidManifest.xml
4. **Run the Application**

## Usage
1. **Sign Up or Log In**
- Create an account or log in to access the chat application.
2. **Start a Chat**
- Search for users and start chatting instantly.
3. **Share Locations**
- Use the location-sharing button to send your current location.
4. **Send Videos**
- Record a video directly in the app and send it to your friends.
5. **Access Received Videos**
- Click on received video messages to play them using the in-app player.
