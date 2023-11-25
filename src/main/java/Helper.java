import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;


public class Helper {
	
	// Function Implementations
	
	// Sorts the list of tasks by due date
	public static List<Task> SortTasks(List<Task> input){
		Collections.sort(input, new DueDateSort());
		return input;
	}
	
	public static boolean validate_user(String email, String passowrd) {
		return true;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		FileInputStream refreshToken = new FileInputStream("todo-list-201/todo-list-201-firebase-adminsdk-jlgqg-a360662814.json");

		FirebaseOptions options = FirebaseOptions.builder()
		    .setCredentials(GoogleCredentials.fromStream(refreshToken))
		    .setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
		    .build();

		FirebaseApp.initializeApp(options);
		
	}

}

class DueDateSort implements Comparator<Task>{

	public int compare(Task t1, Task t2) {
		
		//
		if(t1.dueYear > t2.dueYear) return -1; 
		else if(t1.dueYear == t2.dueYear) {
			if(t1.dueMonth < t2.dueMonth) return -1;
			else if(t1.dueMonth == t2.dueMonth) {
				if(t1.dueDay < t2.dueDay) return -1;
			}
		}
		return 1;
	}
	
}
