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
				errorString = "Category with ID is not found";
				break;
			case -1:
				errorString = "List with ID is not found";

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
