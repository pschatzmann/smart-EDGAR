package ch.pschatzmann.edgar.base.errors;

/**
 * Error information which will be returned by the webservices
 * @author pschatzmann
 *
 */

public class ErrorInformation  {
	private int status;
	private String message;

	public ErrorInformation() {}
	
	public ErrorInformation(int status, String message) {
		this .status = status;
		this.message = message;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
