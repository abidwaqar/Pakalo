# Pakalo - An Artificially Intelligent Cooking Assistant
Pakalo is an Android application designed to help new and amateur cooks in following recipes easily without the hassle of constantly going back and forth through written and video recipes. The application's primary feature is a chatbot which guides a user step by step throughout a recipe, at the user's pace.
The application also features a variety of recipes, each of which is available for a user to start via tha chatbot. Pakalo's chatbot is primarily designed to guide the user through a recipe, and among many intents, the chatbot can search recipes, start and end recipes, search for the quantity of each ingredient, go back and forth through a recipe, and repeat the ingredients of a recipe et cetera.

### Built With
* [RASA](https://rasa.com/) - Framework to build open source conversational chatbots
* [Android Studio](https://developer.android.com/studio) - Development of the application
* [Firebase](https://firebase.google.com/) - NoSQL database with all the recipes
* [Algolia](https://www.algolia.com/doc/guides/sending-and-managing-data/send-and-update-your-data/tutorials/firebase-algolia/) - Searching the Firebase database
* [Anaconda](https://www.anaconda.com/) - For Python enviromnent

### Running Pakalo
To properly run the Pakalo application, you first need to have an Anaconda Python 3 environment set up, and Rasa installed in the environment. You also need to have [ngrok](https://ngrok.com/) installed, this will help us set up a localhost server that Rasa uses into a publicly available webhook. Pakalo needs an internet connection to work.

To run Pakalo,
* Open up two instances of Anaconda prompt and navigate to the environment where rasa is installed as well as the folder with the Rasa project.
* On one prompt run the command to run the rasa action server
```
rasa run actions
```
* On the second prompt run the command to run rasa
```
rasa run --enable-api --cors "*"
```
* Open ngrok and run the command to set up the public server
```
ngrok http 5005
```
* Copy the ngrok https link into the Android project in the RetrofitClientInstance.java file as shown below
```java
private static final String BASE_URL = "https://23e2249874f6.ngrok.io";
```
* Install the Android project onto an Android phone, making sure the rasa servers as well as ngrok is running properly. The app can now be used on the phone.

### Using the application
For the moment, the Android Studio project needs to be installed on an Android device to run the app. Once installed, a user can use their Gmail account to sign up and start using the app. The user can search for recipes, open any one they like, like recipes, and start it on the chatbot.

<img src="https://imgur.com/mULmqMC.png" width="40%" />

<img src="https://imgur.com/t2gV00d.png" width="40%" />

<img src="https://imgur.com/X3IJPX2.png" width="40%" />

<img src="https://imgur.com/am4z8MS.png" width="40%" />

<img src="https://imgur.com/x8eAUI8.png" width="40%" />

<img src="https://imgur.com/yux5pQQ.png" width="40%" />

<img src="https://imgur.com/bS39bOL.png" width="40%" />

### Contributors of the Project
* [Abid Waqar](https://github.com/abidwaqar)
* [Mashood Ur Rehman](https://github.com/LiteralWizard)
* Nauraiz Mushtaq

### Supervisor
Dr. Omer Beg
