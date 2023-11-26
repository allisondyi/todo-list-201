import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/action")
public class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		super.init();
		Helper.InitializeFirestore();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// Build JSON string
		// Read the request body data
		StringBuilder requestJson = new StringBuilder();
		String line = null;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				requestJson.append(line);
			}
		}

		Gson gson = new Gson();

		JsonObject jsonObject = gson.fromJson(requestJson.toString(), JsonObject.class);

		// Retrieve action request
		String requestedAction = jsonObject.get("action").getAsString();
		JsonObject actionData = jsonObject.getAsJsonObject("action_data");

		// Extract individual fields
		String taskName = actionData.has("tname") ? actionData.get("tname").getAsString() : null;
		String taskDescription = actionData.has("tdescription") ? actionData.get("tdescription").getAsString() : null;

		// IMPORTANT!!: Category name and list name are not used for task
		// creation/deletion/modification, use categoryID and listID because we could
		// have same name for lists and categories. These are only used for creating a
		// category and creating a list
		String listName = actionData.has("lname") ? actionData.get("lname").getAsString() : null;
		String categoryName = actionData.has("cname") ? actionData.get("cname").getAsString() : null;
		// Date format DD/MM/YYYY
		// Date can be null, ie. not provided upon task creation
		String taskDueDate = actionData.has("tdate") ? actionData.get("tdate").getAsString() : null;
		//the boolean state if a task is complete or not
		boolean taskStatus = actionData.has("tstatus") ? actionData.get("tstatus").getAsBoolean() : false;


		boolean hasValidAddTaskFields = (taskName != null && taskName.trim().length() > 0 && taskDescription != null
				&& taskDescription.trim().length() > 0);

		int taskID = actionData.has("tID") ? actionData.get("tID").getAsInt() : -1;
		int listID = actionData.has("lID") ? actionData.get("lID").getAsInt() : -1;
		int categoryID = actionData.has("cID") ? actionData.get("cID").getAsInt() : -1;

		switch (requestedAction) {
			// Add task needs taskname, taskdescp, listID (to add to), categoryID (category
			// the list is in)
			case "addTask": {
				if (!hasValidAddTaskFields) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Missing fields required for adding a task"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.AddTask(taskName, taskDescription, taskDueDate, listID, categoryID);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "Category with ID is not found";
						break;
					case -1:
						errorString = "List with ID is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
	
			// Remove task needs taskID, listID and categoryID
			// the list is in)
			case "removeTask": {
	
				// Validate has valid task ID
				if (taskID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Task ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.RemoveTask(taskID, listID, categoryID);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "List with ID is not found";
						break;
					case -1:
						errorString = "category with id is not found / some index out of bound";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// Update task due date needs taskID, listID and categoryID 
			//and a due date input for this specific task
			case "UpdateTaskDueDate": {
				
				// Validate has valid task ID
				if (taskID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Task ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				//validate the task due date
				if (taskDueDate != null && taskDueDate.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("task due date must be > 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateTaskDueDate(taskID, listID, categoryID, taskDueDate);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "task with the 3 ids is not found";
						break;
					case -1:
						errorString = "due date parse error";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// Update task name needs taskID, listID and categoryID 
			//and a task name input for this specific task
			case "UpdateTaskName": {
				
				// Validate has valid task ID
				if (taskID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Task ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				//validate the task due date
				if (taskName != null && taskName.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("task name must be > 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateTaskName(taskID, listID, categoryID, taskName);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "task with the 3 ids is not found";
						break;
					case -1:
						errorString = "task name parse error";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// Update task description needs taskID, listID and categoryID 
			//and a task description input for this specific task
			case "UpdateTaskDescription": {
				
				// Validate has valid task ID
				if (taskID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Task ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				//validate the task due date
				if (taskDescription != null && taskDescription.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("task description must be > 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateTaskDescription(taskID, listID, categoryID, taskDescription);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "task with the 3 ids is not found";
						break;
					case -1:
						errorString = "task descritption parse error";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// Update task completion state needs taskID, listID and categoryID 
			//and a new bool task completion input for this specific task
			case "UpdateTaskCompletionState": {
				
				// Validate has valid task ID
				if (taskID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Task ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateTaskCompletionState(taskID, listID, categoryID, taskStatus);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "task with the 3 ids is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// Add a new list to the category specified, it will require a 
			// category ID and a name for the new list
			case "AddTList": {
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.AddTList(listName, categoryID);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "category with the id is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			
			// update a existing list name, need a category id, list id and
			// a new name for that list
			case "UpdateListName": {
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				if (listName != null && listName.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("list name must be > 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateListName(listID, categoryID, listName);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "list with the id is not found";
						break;
					case -1:
						errorString = "category with the id is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			
			// remove an existing list, need a category id and list id
			case "RemoveList": {
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				// Validate has valid list ID
				if (listID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("List ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.RemoveList(listID, categoryID);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case 0:
						errorString = "list with the id is not found";
						break;
					case -1:
						errorString = "category with the id is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// add category for the user, need category name 
			case "AddCategory": {
				
				//validate category name
				if (categoryName != null && categoryName.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("category name must be > 0"));
					pw.flush();
					return;
				}
				
				// Now we have valid IDs and stuff
				int result = Helper.AddCategory(categoryName);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// update an existing category name, need the id for that category and 
			// a new name for it 
			case "UpdateCategoryName": {
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
				
				//validate category name
				if (categoryName != null && categoryName.trim().length() > 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("category name must be > 0"));
					pw.flush();
					return;
				}

	
				// Now we have valid IDs and stuff
				int result = Helper.UpdateCategoryName(categoryID, categoryName);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case -1:
						errorString = "category with the id is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			// remove an existing list, need a category id and list id
			case "RemoveCategory": {
	
				// Validate has valid category ID
				if (categoryID < 0) {
					// Reject with bad request, return error message as JSON
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					pw.write(gson.toJson("Category ID not valid, must be >= 0"));
					pw.flush();
					return;
				}
	
				// Now we have valid IDs and stuff
				int result = Helper.RemoveCategory(categoryID);
	
				String errorString = "Unexpected Error";
				switch (result) {
					case 1:
						// Respond with User Json
						// Convert the User object to a JSON string
						User cloudUserData = Helper.GetCurrentUserData();
						String cloudUserJson = gson.toJson(cloudUserData);
						pw.write(cloudUserJson);
						pw.flush();
						// Test: Print the JSON string
						System.out.println(cloudUserJson);
						return;
					case -1:
						errorString = "category with the id is not found";
						break;
					case -2:
						errorString = "Failed to sync with cloud";
						break;
				}
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson(errorString));
				pw.flush();
	
				break;
			}
			
			
		}

	}
}
