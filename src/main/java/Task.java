public class Task {
	public String taskName;
	public String taskDescription;
	public int dueDay;
	public int dueMonth;
	public int dueYear;
	public int taskID; //Index within the given todo-List for removal
	public boolean completed;
	public Task(String taskName, String taskDescription, int taskID) {
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.taskID = taskID;
	}
	
	public void SetDueDate(int dueMonth, int dueDay, int dueYear) {
		this.dueMonth = dueMonth;
		this.dueDay = dueDay;
		this.dueYear = dueYear;
	}
	
	public void SetID(int id) {
		taskID = id;
	}
	//We must have a no argument constructor for de-serialization to work
	public Task() {
		
	}
}
