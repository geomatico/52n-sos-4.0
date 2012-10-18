package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ResultTemplate;
import org.n52.sos.request.AbstractServiceRequest;

public interface IGetResultTemplateDAO extends IOperationDAO {
    
    public ResultTemplate getResult(AbstractServiceRequest request) throws OwsExceptionReport;

}
