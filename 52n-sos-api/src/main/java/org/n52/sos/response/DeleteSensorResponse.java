package org.n52.sos.response;

public class DeleteSensorResponse extends AbstractServiceResponse {
    
    private String deletedProcedure;

    public void setDeletedProcedure(String deletedProcedure) {
       this.deletedProcedure = deletedProcedure;
    }

    public String getDeletedProcedure() {
        return deletedProcedure;
    }
}
