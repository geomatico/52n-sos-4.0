package org.n52.sos.convert;

public class ConverterException extends Exception {

    
    /**
     * 
     */
    private static final long serialVersionUID = -8274356164290385880L;

    public ConverterException() {
        super();
    }
    
    public ConverterException(String message) {
        super(message);
    }
    
    public ConverterException(Throwable exception) {
        super(exception);
    }
    
    public ConverterException(String message, Throwable exception) {
        super(message, exception);
    }

}
