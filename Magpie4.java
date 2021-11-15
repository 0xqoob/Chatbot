import java.util.HashMap;
import java.util.Map;

/**
 * A program to carry on conversations with a human user.
 * This version:
 *<ul><li>
 * 		Uses advanced search for keywords 
 *</li><li>
 * 		Will transform statements as well as react to keywords
 *</li></ul>
 * @author Laurie White
 * @version April 2012
 *
 */
public class Magpie4 {
	private HashMap<String, Double> menu = new HashMap<String, Double>() {
		{
			put("Krabby Patty", 1.25);
			put("Double Krabby Patty", 2.00);
			put("Triple Krabby Patty", 3.00);
			put("Krabby Meal", 3.50);
			put("Salty Sea Dog", 1.25);
			put("Footlong", 2.00);
			put("Golden Loaf", 2.00);
			put("Kelp Shake", 2.00);
			put("Seafoam Soda", 1.00);
			put("Kelp Rings", 1.50);
			put("Coral Bits", 1.00);
		}
	};

	private boolean askedName = false;
	private boolean takingOrder = false;

	/**
	 * Get a default greeting 	
	 * @return a greeting
	 */	
	public String getGreeting() {
		return "Welcome to the Krusty Krab, what can I get you today?";
	}
	
	/**
	 * Gives a response to a user statement
	 * 
	 * @param statement
	 *            the user statement
	 * @return a response based on the rules given
	 */
	public String getResponse(String statement) {
		String response = "";

		if (statement.length() == 0) {
			response = "Say something, please.";
		} else if (findKeyword(statement, "hi") >= 0
				|| findKeyword(statement, "hello") >= 0
				|| findKeyword(statement, "hey") >= 0) {
			
			response = "Hey there, what's your name?";
			askedName = true;
		} else if (findKeyword(statement, "no") >= 0) {
			response = "Why so negative?";
		} else if (findKeyword(statement, "mother") >= 0
				|| findKeyword(statement, "father") >= 0
				|| findKeyword(statement, "sister") >= 0
				|| findKeyword(statement, "brother") >= 0) {

			response = "Tell me more about your family.";
		} else if (findKeyword(statement, "order", 0) >= 0) {
			// They want to place an order
			response = "Sounds good! What do you want to order?";
		} else if (findKeyword(statement, "I want to", 0) >= 0) {
			// Responses which require transformations
			response = transformIWantToStatement(statement);
		} else {

			if (askedName) {
				askedName = false;
				return "That's a nice name.";
			}

			// Check if they entered a menu item
			for (Map.Entry<String, Double> entry : menu.entrySet()) {
				String menuItem = entry.getKey();
				double price = entry.getValue();
				
				if (findKeyword(statement, menuItem) >= 0) {
					return String.format(String.format("Alright, that'll be %.2f! Anything else?", price));
				}
			}

			// Look for a two word (you <something> me)
			// pattern
			int psn = findKeyword(statement, "you", 0);

			if (psn >= 0
					&& findKeyword(statement, "me", psn) >= 0) {
				response = transformYouMeStatement(statement);
			} else {
				response = getRandomResponse();
			}
		}

		return response;
	}
	
	/**
	 * Take a statement with "I want to <something>." and transform it into 
	 * "What would it mean to <something>?"
	 * @param statement the user statement, assumed to contain "I want to"
	 * @return the transformed statement
	 */
	private String transformIWantToStatement(String statement) {
		//  Remove the final period, if there is one
		statement = statement.trim();
		String lastChar = statement.substring(statement.length() - 1);

		if (lastChar.equals(".")) {
			statement = statement.substring(0, statement.length() - 1);
		}

		int psn = findKeyword(statement, "I want to", 0);
		String restOfStatement = statement.substring(psn + 9).trim();

		return "What would it mean to " + restOfStatement + "?";
	}
	
	/**
	 * Take a statement with "you <something> me" and transform it into 
	 * "What makes you think that I <something> you?"
	 * @param statement the user statement, assumed to contain "you" followed by "me"
	 * @return the transformed statement
	 */
	private String transformYouMeStatement(String statement) {
		//  Remove the final period, if there is one
		statement = statement.trim();
		String lastChar = statement.substring(statement.length() - 1);

		if (lastChar.equals(".")) {
			statement = statement.substring(0, statement.length() - 1);
		}
		
		int psnOfYou = findKeyword(statement, "you", 0);
		int psnOfMe = findKeyword(statement, "me", psnOfYou + 3);
		
		String restOfStatement = statement.substring(psnOfYou + 3, psnOfMe).trim();

		return "What makes you think that I " + restOfStatement + " you?";
	}
	
	/**
	 * Search for one word in phrase.  The search is not case sensitive.
	 * This method will check that the given goal is not a substring of a longer string
	 * (so, for example, "I know" does not contain "no").  
	 * @param statement the string to search
	 * @param goal the string to search for
	 * @param startPos the character of the string to begin the search at
	 * @return the index of the first occurrence of goal in statement or -1 if it's not found
	 */
	private int findKeyword(String statement, String goal, int startPos) {
		String phrase = statement.trim();
		
		// The only change to incorporate the startPos is in the line below
		int psn = phrase.toLowerCase().indexOf(goal.toLowerCase(), startPos);
		
		//  Refinement--make sure the goal isn't part of a word 
		while (psn >= 0) {
			//  Find the string of length 1 before and after the word
			String before = " ", after = " "; 

			if (psn > 0) {
				before = phrase.substring(psn - 1, psn).toLowerCase();
			}
			
			if (psn + goal.length() < phrase.length()) {
				after = phrase.substring(psn + goal.length(), psn + goal.length() + 1).toLowerCase();
			}
			
			//  If before and after aren't letters, we've found the word
			if (((before.compareTo("a") < 0 ) || (before.compareTo("z") > 0))  //  before is not a letter
					&& ((after.compareTo("a") < 0 ) || (after.compareTo("z") > 0))) {
				return psn;
			}
			
			//  The last position didn't work, so let's find the next, if there is one.
			psn = phrase.indexOf(goal.toLowerCase(), psn + 1);
		}
		
		return -1;
	}
	
	/**
	 * Search for one word in phrase.  The search is not case sensitive.
	 * This method will check that the given goal is not a substring of a longer string
	 * (so, for example, "I know" does not contain "no").  The search begins at the beginning of the string.  
	 * @param statement the string to search
	 * @param goal the string to search for
	 * @return the index of the first occurrence of goal in statement or -1 if it's not found
	 */
	private int findKeyword(String statement, String goal) {
		return findKeyword(statement, goal, 0);
	}

	/**
	 * Pick a default response to use if nothing else fits.
	 * @return a non-committal string
	 */
	private String getRandomResponse() {
		String[] responses = new String[] {
			"Sorry, I don't know how to respond.",
			"Could you repeat that?",
			"Try rephrasing and try again.",
			"Interesting, tell me more.",
			"Hmm.",
			"Do you really think so?",
			"You don't say."
		};

		int NUMBER_OF_RESPONSES = responses.length;
		double r = Math.random();
		int index = (int)(r * NUMBER_OF_RESPONSES);

		return responses[index];
	}

}
