from typing import Any, Text, Dict, List

from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher

import requests
from rasa_sdk import Action
from rasa_sdk.events import SlotSet, FollowupAction
from rasa_sdk.forms import FormAction

import firebase_recipe as fr

recipe_db = fr.Recipe_db()
CurrentRecipe = None

# class ActionHelloWorld(Action):

#     def name(self) -> Text:
#         return "action_hello_world"

#     def run(self, dispatcher: CollectingDispatcher,
#             tracker: Tracker,
#             domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

#         dispatcher.utter_message("Hello World!")

#         return []

####################################################### Start the recipe and take user to step 1
class ActionStartRecipe(Action):

    def name(self) -> Text:
        return "action_start_recipe"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is not None: # If recipe is not selected
            dispatcher.utter_message("Here is the first step of the recipe for you")
            dispatcher.utter_message(CurrentRecipe.get_instruction(0))
            return[SlotSet("recipe_step", str(0))]

        RecipeID = tracker.get_slot("recipe_id")
        if RecipeID == "None" or RecipeID == None:
            dispatcher.utter_message("You need to search for a recipe first")
            return []
            #RecipeID = "k4x7x5MOtWw5Ssee0xab"

        CurrentRecipe = recipe_db.get_recipe(RecipeID)

        dispatcher.utter_message("Ready for another culinary adventure? I am!")
        dispatcher.utter_message("Alright let's start this recipe.")
        INMessage = "The ingredients you need to make " + CurrentRecipe.get_recipe_name() + " are"
        dispatcher.utter_message(INMessage)
        for itr in CurrentRecipe.get_all_ingredients():
            dispatcher.utter_message(itr)

        dispatcher.utter_message("Now let's go to the first step")
        dispatcher.utter_message(CurrentRecipe.get_instruction(0))        

        return [SlotSet("recipe_step", str(0)), SlotSet("recipe_id", RecipeID)]

####################################################### Function to take user to next step
class ActionNextStep(Action):

    def name(self) -> Text:
        return "action_next_step"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]
        
        INStep = tracker.get_slot('recipe_step')
        INStep = int(INStep) + 1

        if INStep >= len(CurrentRecipe.get_all_instructions()): # If no more instructions left
            dispatcher.utter_message("You are on the last step of the recipe. Nothing more to do!")
            return []

        dispatcher.utter_message("Here is the next step of the recipe for you")
        dispatcher.utter_message(CurrentRecipe.get_instruction(INStep))

        return[SlotSet("recipe_step", str(INStep))]

####################################################### Function to take user to previous step
class ActionPreviousStep(Action):

    def name(self) -> Text:
        return "action_previous_step"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]
        
        INStep = tracker.get_slot('recipe_step')
        INStep = int(INStep) - 1

        if INStep < 0: # If there are no steps before
            dispatcher.utter_message("This is the first step. You can't go back. -1 is not a step")
            return []

        dispatcher.utter_message("Here is the previous step of the recipe for you")
        dispatcher.utter_message(CurrentRecipe.get_instruction(INStep))
        
        return[SlotSet("recipe_step", str(INStep))]

####################################################### Function to take user to last step
class ActionLastStep(Action):

    def name(self) -> Text:
        return "action_last_step"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]

        Instructions = CurrentRecipe.get_all_instructions()
        LI = Instructions[-1]

        LIStep = len(Instructions) - 1

        dispatcher.utter_message("Here is the last step of the recipe for you")
        dispatcher.utter_message(LI)
        
        return[SlotSet("recipe_step", LIStep)]

####################################################### Function to repeat the current step
class ActionRepeatStep(Action):

    def name(self) -> Text:
        return "action_repeat_step"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]

        INStep = tracker.get_slot('recipe_step')
        INStep = int(INStep)

        dispatcher.utter_message("Here is the same step again")
        dispatcher.utter_message(CurrentRecipe.get_instruction(INStep))
        
        return[]

####################################################### Function to complete the recipe
class ActionCompleteRecipe(Action):

    def name(self) -> Text:
        return "action_complete_recipe"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]

        CurrentRecipe = None # Setting the current recipe to none
        
        return[SlotSet("recipe_step", None), SlotSet("recipe_id", None)]

####################################################### Action to search a recipe stored in slot recipe
class ActionSearchRecipe(Action):

    def name(self) -> Text:
        return "action_search_recipe"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        if tracker.get_slot("recipe") == None or tracker.get_slot("recipe") == "None":
            dispatcher.utter_message("I'm sorry that is an invalid keyword")
            return []

        SearchedR = recipe_db.search(tracker.get_slot('recipe'))

        if len(SearchedR) < 1: # No recipes found
            dispatcher.utter_message("Unfortunately we do not have any recipes for that")
            return [SlotSet("recipe", None)]

        #print(SearchedR)

        dispatcher.utter_message("Here are your searched recipes")

        buttons = []
        for itr in SearchedR:
            payload = "/recipe_id_from_button{\"recipe_id\":\"" + itr[1] + "\"}"
            buttons.append({"title": itr[0], "payload": payload})

        dispatcher.utter_button_message("Here are the recipes I found", buttons) # Presenting recipes as buttons

        return [SlotSet("recipe", None)]

####################################################### # Function to repeat the ingredients
class ActionTellIngredients(Action):

    def name(self) -> Text:
        return "action_tell_ingredients"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None)]

        INMessage = "The ingredients you need to make " + CurrentRecipe.get_recipe_name() + " are"
        dispatcher.utter_message(INMessage)
        for itr in CurrentRecipe.get_all_ingredients():
            dispatcher.utter_message(itr)
        
        return []

####################################################### Form to take the search QnA question input from user
class QuestionForm(FormAction):

    def name(self) -> Text:
        """Unique identifier of the form"""

        return "general_question_form"

    @staticmethod
    def required_slots(tracker: Tracker) -> List[Text]:
        """A list of required slots that the form has to fill"""

        return ["question"]

    def slot_mappings(self) -> Dict[Text, Any]:
        return {"question": self.from_text(not_intent = "change_mind")}

    def submit(self,
               dispatcher: CollectingDispatcher,
               tracker: Tracker,
               domain: Dict[Text, Any]
               ) -> List[Dict]:

        question = tracker.get_slot('question')

        print(question)

        message = "You want to ask " + question
        dispatcher.utter_message(message)

        return [SlotSet("question", None)]

####################################################### Form to get recipe that was not recognized
class RecipeForm(FormAction):

    def name(self) -> Text:
        """Unique identifier of the form"""

        return "recipe_form"

    @staticmethod
    def required_slots(tracker: Tracker) -> List[Text]:
        """A list of required slots that the form has to fill"""

        return ["recipe"]

    def slot_mappings(self) -> Dict[Text, Any]:
        return {"recipe": self.from_text(intent = None)}

    def submit(self,
               dispatcher: CollectingDispatcher,
               tracker: Tracker,
               domain: Dict[Text, Any]
               ) -> List[Dict]:

        recipeN = tracker.get_slot('recipe')

        print(recipeN)

        #dispatcher.utter_template("utter_your_recipe", tracker)

        return []

####################################################### Form to get ingredient 
class IngredientQuantityForm(FormAction):

    def name(self) -> Text:
        """Unique identifier of the form"""

        return "ingredient_quantity_form"

    @staticmethod
    def required_slots(tracker: Tracker) -> List[Text]:
        """A list of required slots that the form has to fill"""

        return ["ingredient"]

    def slot_mappings(self) -> Dict[Text, Any]:
        return {"ingredient": self.from_text(intent = None)}

    def submit(self,
               dispatcher: CollectingDispatcher,
               tracker: Tracker,
               domain: Dict[Text, Any]
               ) -> List[Dict]:

        ingredient = tracker.get_slot('ingredient')

        print(ingredient)

        global CurrentRecipe
        if CurrentRecipe is None: # If recipe is not selected
            dispatcher.utter_message("You do not have a current recipe running")
            return [SlotSet("recipe_step", None), SlotSet("recipe_id", None), SlotSet("ingredient", None)]

        IGs = CurrentRecipe.get_ingredient(ingredient)

        if len(IGs) <= 0:
            dispatcher.utter_message("I could not find that ingredient, sorry")
            return [SlotSet("ingredient", None)]
         
        dispatcher.utter_message("I'll repeat the ingredients you asked for")
        for itr in IGs:
            dispatcher.utter_message(itr)

        return [SlotSet("ingredient", None)]
