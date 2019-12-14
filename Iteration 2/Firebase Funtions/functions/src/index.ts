import * as functions from 'firebase-functions';
import * as admin from "firebase-admin";
admin.initializeApp(functions.config().firebase);
const env = functions.config();

import * as algoliasearch from 'algoliasearch';

//initialize algolia client
const client = algoliasearch(env.algolia.appid, env.algolia.apikey);
const index = client.initIndex('recipes');
let db = admin.firestore();


//updating algolia after adding recipe
exports.indexRecipe = functions.firestore.document('recipes/{recipeID}').onCreate((snap, context) => {
    // const recipeTitle = snap.get('title');
    const objectID = snap.id;
    // const recipeTitle = snap.get;
    var objects;
    var recipeTitle;
    db.collection("recipes").doc(objectID).get().then(doc => {
        if (doc.exists) {
            recipeTitle = doc.get('title');
            objects = {
                objectID: objectID,
                recipeTitle: recipeTitle
            }
            index.addObject(objects,(err,content)=>{
                // console.log(content)
            })
        } 
        else{
            // console.log("Document doesn't exist");
        }
    }).catch(function(error) {
    // console.log("Error getting document:", error);
    });
    return 0;
});


//updating algolia after removing recipe
exports.unindexRecipe = functions.firestore.document('recipes/{recipeID}').onDelete((snap, context) => {
    const objectID = snap.id;

    //deleting data from algolia
    return index.deleteObject(objectID);
});