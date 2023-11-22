import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
	
	
	
	public static void main(String[] args) {
		
		
	}

}

class DueDateSort implements Comparator<Task>{

	public int compare(Task t1, Task t2) {
		
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
