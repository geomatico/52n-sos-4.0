package org.n52.sos.ds;

import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;

public interface IInsertObservationDAO extends IOperationDAO {
    
    public int insertObservation(SosObservationCollection observation) throws OwsExceptionReport;

}
