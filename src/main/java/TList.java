import java.util.ArrayList;
import java.util.List;


//Data structure for a todo-list, which contains a list of tasks within the list
public class TList {
	public String listName;
	public List<Task> tasks = new ArrayList<Task>();
	
	//We must have a no argument constructor for de-serialization to work
	public TList() {
		
	}
	
	public TList(String listName) {
		this.listName = listName;
	}
	
	public void AddTask(Task t) {
		tasks.add(t);
	}
}
