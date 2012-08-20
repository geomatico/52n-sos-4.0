package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OwsExceptionReport;

public interface IDataSourceInitializator {

   public void initializeDataSource() throws OwsExceptionReport;

}
