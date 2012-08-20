package org.n52.sos.ogc.ows;

import org.n52.sos.exception.IExceptionCode;


public class OwsException {
    
    private IExceptionCode code;
    
    private String locator;
    
    private String[] messages;
    
    private Exception exception;
    

    public OwsException(IExceptionCode code, String locator, String[] messages) {
       this.code = code;
       this.locator = locator;
       this.messages = messages;
    }

    public OwsException(IExceptionCode code, String locator, Exception exception) {
        this.code = code;
        this.locator = locator;
        this.exception = exception;
    }

    public OwsException(IExceptionCode code, String locator, String[] messages, Exception exception) {
        this.code = code;
        this.locator = locator;
        this.messages = messages;
        this.exception = exception;
    }

    public IExceptionCode getCode() {
        return code;
    }

    public String getLocator() {
        return locator;
    }

    public String[] getMessages() {
        return messages;
    }

    public Exception getException() {
        return exception;
    }

}
