public class Task {
	public String taskName;
	public String taskDescription;
	public int dueDay;
	public int dueMonth;
	public int dueYear;
	
	public Task(String taskName, String taskDescription, int dueMonth, int dueDay, int dueYear) {
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.dueMonth = dueMonth;
		this.dueDay = dueDay;
		this.dueYear = dueYear;
	}
}
