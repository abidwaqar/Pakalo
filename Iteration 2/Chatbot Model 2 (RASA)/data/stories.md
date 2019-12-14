## user greets pakalo and wants to cook
* greet
  - utter_greet
  - utter_wanna_cook
* affirm
  - utter_ask_what_to_cook
* search_recipe{"recipe": "pizza"}
  - utter_ok
  - utter_your_recipe
  - action_search_recipe
  - slot{"recipe": null}

## user greets pakalo and wants to cook but does not give recipe name
* greet
  - utter_greet
  - utter_wanna_cook
* affirm
  - utter_ask_what_to_cook
* search_recipe
  - recipe_form
  - form{"name": "recipe_form"}
  - form{"name": null}
  - utter_ok
  - utter_your_recipe
  - action_search_recipe
  - slot{"recipe": null}

## user greets and does not want to cook
* greet
  - utter_greet
  - utter_wanna_cook
* deny
  - utter_ok
  - utter_i_can_help

## user wants to search for recipe
* search_recipe{"recipe": "pizza"}
  - utter_ok
  - utter_your_recipe
  - action_search_recipe
  - slot{"recipe": null}

## user wants to search for recipe but does not give name
* search_recipe
  - recipe_form
  - form{"name": "recipe_form"}
  - form{"name": null}
  - utter_ok
  - utter_your_recipe
  - action_search_recipe
  - slot{"recipe": null}

## user wants to go to the next step
* next_step
  - utter_ok
  - utter_nextStep
  - action_next_step

## user wants to go to the prev step
* previous_step
  - utter_ok
  - utter_prevStep
  - action_previous_step

## user wants to go to the first step
* first_step
  - utter_ok
  - action_start_recipe

## user wants to go to the last step
* last_step
  - utter_ok
  - utter_lastStep
  - action_last_step

## complete recipe
* complete_recipe
  - utter_ok
  - utter_complete
  - action_complete_recipe

## user wants to repeat the step
* repeat_step
  - utter_ok
  - utter_repeatStep
  - action_repeat_step

## say goodbye
* goodbye
  - utter_i_can_help
  - utter_goodbye

## user has a question happy path
* ask_question
  - general_question_form
  - form{"name": "general_question_form"}
  - form{"name": null}

## user has a question but does not follow through
* ask_question
  - general_question_form
  - form{"name": "general_question_form"}
* change_mind
  - utter_confirm
* affirm
  - action_deactivate_form
  - form{"name": null}
  - utter_ok

## user has a question but wants to stop but then does not stop
* ask_question
  - general_question_form
  - form{"name": "general_question_form"}
* change_mind
  - utter_confirm
* deny
  - utter_ask_question
  - form{"name": "general_question_form"}
  - form{"name": null}

## user has to ask what ingredients they need
* tell_ingredients
  - utter_ok
  - action_tell_ingredients

## user needs to know the ingredient quantity
* ingredient_quantity
  - ingredient_quantity_form
  - form{"name": "ingredient_quantity_form"}
  - form{"name": null}

## user wants to know what Pakalo is
* who_are_you
  - utter_iamabot
  - utter_icando
  - utter_whomademe

## fallback story
* out_of_scope
  - action_default_fallback

## getting specific recipe from button
* recipe_id_from_button
  - utter_ok
  - utter_telltostart