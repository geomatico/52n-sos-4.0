package org.n52.sos.convert;

public class ConverterKeyType implements Comparable<ConverterKeyType> {

    private String fromNamespace;

    private String toNamespace;

    public ConverterKeyType(String fromNamespace, String toNamespace) {
        super();
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
    }

    public String getFromNamespace() {
        return fromNamespace;
    }

    public String getToNamespace() {
        return toNamespace;
    }

    @Override
    public int compareTo(ConverterKeyType o) {
        if (o instanceof ConverterKeyType) {
            ConverterKeyType toCheck = (ConverterKeyType) o;
            if (checkParameter(fromNamespace, toCheck.fromNamespace)
                    && checkParameter(toNamespace, toCheck.toNamespace)) {
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
        if (paramObject instanceof ConverterKeyType) {
            ConverterKeyType toCheck = (ConverterKeyType) paramObject;
            return (checkParameter(fromNamespace, toCheck.fromNamespace) && checkParameter(toNamespace,
                    toCheck.toNamespace));
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
        if (fromNamespace != null) {
            hash = 31 * hash + fromNamespace.hashCode();
        }
        if (toNamespace != null) {
            hash += 31 * hash + toNamespace.hashCode();
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
        string.append(fromNamespace + ",");
        string.append(toNamespace);
        string.append(")");
        return string.toString();
    }

}
