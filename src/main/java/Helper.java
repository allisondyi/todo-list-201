import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.coyote.http11.filters.VoidInputFilter;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Helper {
	static Firestore db;
	static boolean _initialized;
	static User currentUserPtr;
	// Function Implementations

	///////////// Core Functions (Offline: Sorting)//////////

	// Sorts the list of tasks by due date
	public static List<Task> SortTasks(List<Task> input) {
		Collections.sort(input, new DueDateSort());
		return input;
	}

	// Returns 1 for success, 0 for no user with email found, -1 for incorrect
	// password
	//Initilizes currentUserPtr if success
	public static int AuthenticateLogin(String email, String passowrd) {
		DocumentReference docRef = db.collection("users").document(email);
		// asynchronously retrieve the document
		ApiFuture<DocumentSnapshot> future = docRef.get();
		// block on response
		DocumentSnapshot document = null;
		try {
			document = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (document.exists()) {
			// convert document to POJO
			User temp = document.toObject(User.class);
			if (temp.password.equals(passowrd)) {
				InternalInitializeUser(email);
				return 1;
			}
			return -1;
		} else {
			return 0;
		}
	}

	/////////////////////////////////////////////////////////
	public static User GetCurrentUserData() {
		return currentUserPtr;
	}
	// Tests: Initializes Firestore database and sets global db reference
	//Important! Due to servlet life cycle, we will have a init function called by servlet instead of main
	//This main function was for pure debug purposes
	public static void main(String[] args) throws IOException {
		//System.out.println("Starting in main Helper");
		//Test for initialization
		//InitializeFirestore();

		// Important: Commented out code are test functions, do not remove or uncomment
		// them
		// This was a test for initializing the current user ptr
		// InternalInitializeUser("fuck@gmail.com");

		// This was a test for deletion
		// currentUserPtr.categories.clear();

		// This was a test for modification sync
		// InternalSyncUSer();

		// This was a test for add user
		// AddUser("fuck@gmail.com", "Kevin", "Yang", "123");
	}

	//Core: Starts Firestore
	public static void InitializeFirestore() {
		if (_initialized)
			return;
		System.out.println("Initializing Firestore...");
		FileInputStream refreshToken = null;
		try {
			refreshToken = new FileInputStream("todo-list-201-firebase-adminsdk-jlgqg-a360662814.json");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		FirebaseOptions options = null;
		try {
			options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(refreshToken)).build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		FirebaseApp.initializeApp(options);
		db = FirestoreClient.getFirestore();
		_initialized = true;
	}

///////////// Database Firestore stuff///////////////////////

	// Core Sync function, syncs memory cached User data with Firestore
	public static boolean SyncUserChanges() {
		if (currentUserPtr == null)
			return false;

		// Uploads our cached user data to Firestore
		ApiFuture<WriteResult> future = db.collection("users").document(currentUserPtr.email).set(currentUserPtr);
		// block on response if required
		try {
			System.out.println("Update User: " + currentUserPtr.email + " " + future.get().getUpdateTime());
			return true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	// Adds a user to Firestore database, when success sets currentUserPtr to it
	public static void AddUser(String email, String fname, String lname, String password) {

		// Reference to the 'users' collection
		CollectionReference users = db.collection("users");
		// Create a new user object
		User newUser = new User(email, fname, lname, password);

		ApiFuture<WriteResult> future = db.collection("users").document(email.trim()).set(newUser);
		// block on response if required
		try {
			System.out.println("Update time : " + future.get().getUpdateTime());
			InternalInitializeUser(email);
			System.out.println("Successfully created user, current user set");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

	// This is called to set the currentUserPtr
	// Gets user data from Firestore, this assues that we have already authenticated
	// user thus we are getting by
	// document ID, which is just the user's email
	public static boolean InternalInitializeUser(String email) {
		DocumentReference docRef = db.collection("users").document(email);
		// asynchronously retrieve the document
		ApiFuture<DocumentSnapshot> future = docRef.get();
		// block on response
		DocumentSnapshot document = null;
		try {
			document = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (document.exists()) {
			// convert document to POJO
			currentUserPtr = document.toObject(User.class);
			System.out.println("Successfully initialized user with email " + email);
			System.out.println(currentUserPtr);
			return true;
		} else {
			System.out.println("User not found! Email: " + email);
		}
		return false;
	}

/////////////////////////////////////////////////////////////

}

// Comparator class
class DueDateSort implements Comparator<Task> {

	public int compare(Task t1, Task t2) {

		//
		if (t1.dueYear > t2.dueYear)
			return -1;
		else if (t1.dueYear == t2.dueYear) {
			if (t1.dueMonth < t2.dueMonth)
				return -1;
			else if (t1.dueMonth == t2.dueMonth) {
				if (t1.dueDay < t2.dueDay)
					return -1;
			}
		}
		return 1;
	}

}
