package org.n52.sos.encode;

public class EncoderKeyType implements Comparable<EncoderKeyType> {

    private String namespace;

    private String service;

    private String version;

    public EncoderKeyType(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public int compareTo(EncoderKeyType o) {
        if (o instanceof EncoderKeyType) {
            EncoderKeyType toCheck = (EncoderKeyType) o;
            if (checkParameter(service, toCheck.service) && checkParameter(version, toCheck.version)
                    && checkParameter(namespace, toCheck.namespace)) {
                return 0;
            }
            return 1;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof EncoderKeyType) {
            EncoderKeyType toCheck = (EncoderKeyType) paramObject;
            return (checkParameter(service, toCheck.service) && checkParameter(version, toCheck.version) && checkParameter(
                    namespace, toCheck.namespace));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        if (service != null) {
            hash = 31 * hash + service.hashCode();
        }
        if (version != null) {
            hash += 31 * hash + version.hashCode();
        }
        if (namespace != null) {
            hash += 31 * hash + namespace.hashCode();
        }
        return hash;
    }

    private boolean checkParameter(String localParameter, String parameterToCheck) {
        if ((localParameter == null && parameterToCheck == null)
                || (localParameter != null && parameterToCheck != null && localParameter.equals(parameterToCheck))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("EncoderKey(");
        string.append(service + ",");
        string.append(version + ",");
        string.append(namespace);
        string.append(")");
        return string.toString();
    }

}
