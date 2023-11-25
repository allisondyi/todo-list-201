
//This is the User data structure for Firestore object

import java.util.ArrayList;
import java.util.List;

public class User {
	public String email;
	public String fname;
	public String lname;
	public String password;
	
	public List<Category> categories = new ArrayList<Category>();
	
	public User(String email, String fname, String lname, String password) {
		this.email = email;
		this.fname = fname;
		this.lname = lname;
		this.password = password;
	}
	
	//We must have a no argument constructor for de-serialization to work
	public User() {
		
	}
	
	public void AddCategory(String categoryName, List<TList> todoLists) {
		categories.add(new Category(categoryName, todoLists));
	}
}
