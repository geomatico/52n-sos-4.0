package org.n52.sos.decode;

public class DecoderKeyType implements Comparable<DecoderKeyType> {

    private String namespace;

    private String service;

    private String version;

    public DecoderKeyType(String namespace) {
        this.namespace = namespace;
    }

    public DecoderKeyType(String service, String version) {
        this.service = service;
        this.version = version;
    }

    public String getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(DecoderKeyType o) {
        if (o instanceof DecoderKeyType) {
            DecoderKeyType toCheck = (DecoderKeyType) o;
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
        if (paramObject instanceof DecoderKeyType) {
            DecoderKeyType toCheck = (DecoderKeyType) paramObject;
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
        string.append("DecoderKey(");
        string.append(service + ",");
        string.append(version + ",");
        string.append(namespace);
        string.append(")");
        return string.toString();
    }
}
