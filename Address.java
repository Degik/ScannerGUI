public class Address {
	
	private String name;
	private String host;
	private int addressId;
	private boolean status;
	private int typeErr;
	
	public Address() {
		//
	}
	
	public Address(String name, String host, int addressId, boolean status) {
		if(name == null || host == null) {
			throw new IllegalArgumentException();
		}
		this.name = name;
		this.host = host;
		this.addressId = addressId;
		this.status = status;
	}
	
	public int getAddressId() {
		return addressId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHost() {
		return host;
	}
	
	public boolean getStatus() {
		return status;
	}
	
	public String statusString() {
		if(status) {
			return "ONLINE";
		} else {
			return "OFFLINE";
		}
	}
	
	public void setTypeErr(int typeErr) {
		this.typeErr = typeErr;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() {
		return addressId;
	}
	
	@Override
	public String toString() {
		String statusStr;
		String typeErrStr;
		switch(typeErr) {
		case 1:
			typeErrStr = "Connessione non riuscita";
			break;
		case 2:
			typeErrStr = "Errore di connessione (time out)";
			break;
		case 3:
			typeErrStr = "Nessuno";
			break;
		default:
			typeErrStr = "Errore";
		}
		
		if(status) {
			statusStr = "ONLINE";
		} else {
			statusStr = "OFFLINE";
		}
		String s = "[" + name + "  " + host + " Id: " + addressId + "  " + statusStr + "  Errore: " + typeErrStr + "]";
		return s;
	}
}
