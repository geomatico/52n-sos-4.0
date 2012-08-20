package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;

public interface IInsertObservationDAO extends IOperationDAO {

    public InsertObservationResponse insertObservation(InsertObservationRequest request) throws OwsExceptionReport;

}
