package ch.pschatzmann.edgar.base.errors;

public class DataLoadException extends Exception {
	private static final long serialVersionUID = 1L;

	public DataLoadException(Exception ex){
		super(ex);
	}
}
