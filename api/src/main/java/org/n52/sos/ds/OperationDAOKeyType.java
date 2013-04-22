package org.n52.sos.ds;


public class OperationDAOKeyType implements Comparable<OperationDAOKeyType> {
    
    private String operationName;
    private String service;
    
    public OperationDAOKeyType() {
        super();
    }

    public OperationDAOKeyType(String service, String operationName) {
        super();
        this.service = service;
        this.operationName = operationName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public int compareTo(OperationDAOKeyType o) {
        if (o instanceof OperationDAOKeyType) {
            if (service.equals(o.service) && operationName.equals(o.operationName)) {
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
        if (service != null && operationName != null && paramObject instanceof OperationDAOKeyType) {
            OperationDAOKeyType toCheck = (OperationDAOKeyType) paramObject;
            return (service.equals(toCheck.service) && operationName.equals(toCheck.operationName));
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
        final int prime = 31;
        int hash = 7;
        hash = prime * hash + ((this.service != null) ? this.service.hashCode() : 0);
        hash = prime * hash + ((this.operationName != null) ? this.operationName.hashCode() : 0);
        return hash;
    }

}
