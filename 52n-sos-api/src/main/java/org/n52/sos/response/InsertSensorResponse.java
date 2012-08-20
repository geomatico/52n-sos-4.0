package org.n52.sos.response;

public class InsertSensorResponse extends AbstractServiceResponse {
    
    private String assignedProcedure;
    
    private String assignedOffering;

    public void setAssignedProcedure(String assignedProcedure) {
        this.assignedProcedure = assignedProcedure;
    }
    
    public String getAssignedProcedure() {
        return assignedProcedure;
    }

    public void setAssignedOffering(String assignedOffering) {
        this.assignedOffering = assignedOffering;
    }
    
    public String getAssignedOffering() {
        return assignedOffering;
    }

}
