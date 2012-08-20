package org.n52.sos.response;

public class UpdateSensorResponse extends AbstractServiceResponse {
    
    private String updatedProcedure;

    public void setUpdatedProcedure(String updatedProcedure) {
        this.updatedProcedure = updatedProcedure;
    }

    public String getUpdatedProcedure() {
        return updatedProcedure;
    }
}
