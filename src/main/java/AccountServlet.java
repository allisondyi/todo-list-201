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

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
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
		JsonObject userData = jsonObject.getAsJsonObject("user_data");

		// Extract individual fields
		String firstname = userData.has("fname") ? userData.get("fname").getAsString() : null;
		String lastname = userData.has("lname") ? userData.get("lname").getAsString() : null;
		String email = userData.has("email") ? userData.get("email").getAsString() : null;
		String password = userData.has("password") ? userData.get("password").getAsString() : null;

		boolean hasValidLoginCredentials = (email != null && email.trim().length() > 0 && password != null
				&& password.trim().length() > 0);

		boolean hasValidUserCreationCredentials = (firstname != null && firstname.trim().length() > 0
				&& lastname != null && lastname.trim().length() > 0);

		switch (requestedAction) {
		case "login":
			if (!hasValidLoginCredentials) {
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson("Invalid Provided Login Credentials: Check email and password"));
				pw.flush();
				return;
			}

			// Here we have valid credentials
			// Validate if user with this email exists
			int result = Helper.AuthenticateLogin(email, password);
			if (result == 1) {
				response.setStatus(HttpServletResponse.SC_OK);
				// Respond with User Json
				// Convert the User object to a JSON string
				User cloudUserData = Helper.GetCurrentUserData();
				String cloudUserJson = gson.toJson(cloudUserData);
				pw.write(cloudUserJson);
				pw.flush();
				// Test: Print the JSON string
				System.out.println(cloudUserJson);
			} else {
				// Failed to authenticate
				response.setStatus(HttpServletResponse.SC_OK);
				String errorString = "No Registered Email";
				if (result == -1) {
					errorString = "Incorrect Password";
				}
				pw.write(gson.toJson(errorString));
				pw.flush();
				return;
			}
			break;

		case "signup":
			if (!hasValidLoginCredentials) {
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson("Invalid Provided Login Credentials: Check email and password"));
				pw.flush();
				return;
			}
			if (!hasValidUserCreationCredentials) {
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson("Invalid Provided User Credentials: Check fname and lname"));
				pw.flush();
				return;
			}

			// User has provided all valid details
			// Check if no existing user, then proceed to register

			if (Helper.AuthenticateLogin(email, password) == 0) {
				// No user with this email found, we can proceed to register
				Helper.AddUser(email, firstname, lastname, password);

				// Note: Potential race condition? If fails could be due to adding user ->
				// getting from cloud instead of immediate local cache
				response.setStatus(HttpServletResponse.SC_OK);
				// Respond with User Json
				// Convert the User object to a JSON string
				User cloudUserData = Helper.GetCurrentUserData();
				String cloudUserJson = gson.toJson(cloudUserData);
				pw.write(cloudUserJson);
				pw.flush();
				// Test: Print the JSON string
				System.out.println(cloudUserJson);
			}
			else {
				//User with email already exists
				// Reject with bad request, return error message as JSON
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.write(gson.toJson("User with email already exists"));
				pw.flush();
				return;
			}
			break;
		}

	}
}
