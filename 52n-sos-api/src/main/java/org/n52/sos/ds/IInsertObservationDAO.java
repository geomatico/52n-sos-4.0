package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.InsertObservationRequest;

public interface IInsertObservationDAO extends IOperationDAO {
    
    public int insertObservation(InsertObservationRequest request) throws OwsExceptionReport;

}
