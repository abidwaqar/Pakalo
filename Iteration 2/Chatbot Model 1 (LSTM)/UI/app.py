from flask import Flask, render_template, request
from algoliasearch.search_client import SearchClient
import firebase_admin
import google.cloud
from firebase_admin import credentials, firestore
app = Flask(__name__)

    
client = SearchClient.create('YourApplicationID', 'YourAdminAPIKey' )
indexFireStore = client.init_index('recipes')
cred = credentials.Certificate("./pakalo-abid786-firebase-adminsdk-xc8ts-ad52022ec7.json")
fbapp = firebase_admin.initialize_app(cred)
db = firestore.client()
@app.route("/")
def home():
    return render_template("home.html")



        

if __name__ == "__main__":
    app.run()

    
