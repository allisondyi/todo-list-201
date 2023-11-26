import java.util.ArrayList;
import java.util.List;

//Data structure for category, a category contains list of todo-lists (for example: USC cateogry contains todo lists for diff classes)
public class Category {
	public String categoryName;
	public int categoryID;
	public List<TList> tlists = new ArrayList<TList>();
	
	public Category(String categoryName, List<TList> todoLists) {
		this.categoryName = categoryName;
		
		if(todoLists == null || todoLists.size() == 0) {
			return;
		}
		
		this.tlists = todoLists;
	}
	
	public void SetID(int id) {
		categoryID = id;
	}
	//We must have a no argument constructor for de-serialization to work
	public Category() {
		
	}
}
