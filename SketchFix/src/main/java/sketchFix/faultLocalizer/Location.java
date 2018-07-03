/**
 * @author Lisa Apr 10, 2017 Location.java 
 */
package sketchFix.faultLocalizer;

public class Location {
	private String filePath;
	private String method;
	private int location;
	private int id;

	public Location(String f, String mtd, int loc, int id) {
		filePath = f;
		method = mtd;
		location = loc;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
