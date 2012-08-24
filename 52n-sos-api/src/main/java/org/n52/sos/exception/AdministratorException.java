package org.n52.sos.exception;

public class AdministratorException extends Exception {

    private static final long serialVersionUID = 1L;
    
   public AdministratorException(String message) {
       super(message);
   }
   
   public AdministratorException(Throwable exception) {
       super(exception);
   }
   
   public AdministratorException(String message, Throwable exception) {
       super(message, exception);
   }

}
