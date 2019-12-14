import firebase_admin
import google.cloud
from firebase_admin import credentials, firestore
from algoliasearch.search_client import SearchClient

    
class Recipe():
    def __init__(self, recipe_name, recipe_ingredients, recipe_instructions):
        self.recipe_name = recipe_name
        self.recipe_ingredients = recipe_ingredients
        self.recipe_instructions = recipe_instructions
    
    def get_recipe_name(self):
        return self.recipe_name
    
    def get_all_ingredients(self):
        return self.recipe_ingredients
    
    def get_all_instructions(self):
        return self.recipe_instructions
        
    def get_instruction(self, index):
        if index >= len(self.recipe_instructions):
            return "There are only " + str(len(self.recipe_instructions)) + " instructions for this recipe"
        return self.recipe_instructions[index]

    def get_ingredient(self, ingredient_name):
        return [ingredient for ingredient in self.recipe_ingredients if ingredient_name in ingredient]        

class Recipe_db():
    def __init__(self):
        client = SearchClient.create('RONI3GVMZF', 'd069d6b6b79085dc8fce9e619feed841')
        self.algolia_recipe_db = client.init_index('recipes')
        
        cred = credentials.Certificate("./pakalo-abid786-firebase-adminsdk-xc8ts-ad52022ec7.json")
        app = firebase_admin.initialize_app(cred)
        self.firestore_recipe_db = firestore.client().collection('recipes')
    
    def search(self, recipe_substring):
        '''returns list of tuple of search string with recipe name and objectID'''
        search_results = self.algolia_recipe_db.search(recipe_substring)
        return [(i['recipeTitle'], i["objectID"]) for i in search_results["hits"]]
        
    def get_recipe(self, object_id):
        recipe_dict = self.firestore_recipe_db.document(object_id).get().to_dict()
        return Recipe(recipe_dict['title'], recipe_dict['ingredients'], recipe_dict['instructions'])
    
            
# recipe_db = Recipe_db()
# searches = recipe_db.search("chocolate")
# print (searches)
# recipe = recipe_db.get_recipe(searches[0][1])
# print (recipe.get_recipe_name())
# print (recipe.get_all_ingredients())
# print (recipe.get_all_instructions())
# print (recipe.get_instruction(3))
# print (recipe.get_instruction(59))