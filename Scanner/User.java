
public class User {
	private String sender;
	private String password;
	private String recipient;
	
	public User() {
		//
	}
	public User(String sender, String password, String recipient) {
		this.sender = sender;
		this.password = password;
		this.recipient = recipient;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getRecipient() {
		return recipient;
	}
}
