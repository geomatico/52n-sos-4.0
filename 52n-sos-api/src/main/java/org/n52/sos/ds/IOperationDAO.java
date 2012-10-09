/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;

/**
 * Interface for all SOS operation DAOs
 * 
 */
public interface IOperationDAO {

    /**
     * Get the SOS operation name this DAO supports
     * 
     * @return The supported SOS operation name
     */
    public String getOperationName();

    /**
     * Get the OperationsMetadata of the supported SOS operation for the
     * capabilities
     * 
     * @param version
     *            SOS version
     * @param connection
     *            Data source connection
     * @return OperationsMetadata for the operation
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport;
    
    public IExtension getExtension(Object connection) throws OwsExceptionReport;
}