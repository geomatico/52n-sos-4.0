package org.n52.sos.service.admin;

public class AdministratoConstants {
    
    /** enum with parameter names for getCapabilities request */
    public enum AdministatorParams {
        service, request;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (s.equals(AdministatorParams.service.name()))
                            || (s.equals(AdministatorParams.request.name()));
            return contained;
        }
    }

}
