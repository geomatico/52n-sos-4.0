/**
 * Copyright (C) 2013
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
package org.n52.sos.ds.hibernate;

import java.util.List;
import java.util.Map;

import org.n52.sos.binding.Binding;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ds.OperationDAO;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.ListMultiMap;
import org.n52.sos.util.MultiMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carsten Hollmann
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @author Christian Autermann <c.autermann@52north.org>
 * 
 * @since 4.0.0
 *
 */
@Deprecated
public abstract class AbstractHibernateOperationDao implements OperationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHibernateOperationDao.class);

    @Override
    public OWSOperation getOperationsMetadata(String service, String version) throws OwsExceptionReport {
        Map<String, List<String>> dcp =  getDCP(new OperationDecoderKey(service, version, getOperationName()));
        if (dcp == null || dcp.isEmpty()) {
            LOGGER.debug("Operation {} not available due to empty DCP map.", getOperationName());
            return null;
        }
        OWSOperation operation = new OWSOperation();
        operation.setDcp(dcp);
        operation.setOperationName(getOperationName());
        setOperationsMetadata(operation, service, version);
        return operation;
    }

    @Override
    /* provide a default implementation for extension-less DAO's */
    public SwesExtension getExtension() throws OwsExceptionReport {
        return null;
    }

    protected ContentCache getCache() {
        return getConfigurator().getCache();
    }

    protected Configurator getConfigurator() {
        return Configurator.getInstance();
    }

	/**
     * Get the HTTP DCPs for a operation
     *
     * @param decoderKey the decoderKey
     * @return Map with DCPs for the SOS operation

     *
     * @throws OwsExceptionReport
     */
    protected Map<String, List<String>> getDCP(OperationDecoderKey decoderKey) throws OwsExceptionReport {
        ListMultiMap<String, String> dcp = MultiMaps.newListMultiMap();
        String serviceURL = Configurator.getInstance().getServiceURL();
        try {
            for (Binding binding : Configurator.getInstance().getBindingRepository().getBindings().values()) {
                String url = serviceURL + binding.getUrlPattern();
                if (binding.checkOperationHttpGetSupported(decoderKey)) {
                    dcp.add(SosConstants.HTTP_GET, url + "?");
                }
                if (binding.checkOperationHttpPostSupported(decoderKey)) {
                    dcp.add(SosConstants.HTTP_POST, url);
                }
                if (binding.checkOperationHttpPutSupported(decoderKey)) {
                    dcp.add(SosConstants.HTTP_PUT, url);
                }
                if (binding.checkOperationHttpDeleteSupported(decoderKey)) {
                    dcp.add(SosConstants.HTTP_DELETE, url);
                }
            }
        } catch (Exception e) {
            // FIXME valid exception
            throw new NoApplicableCodeException().causedBy(e);
        }
        return dcp;
    }

    protected abstract void setOperationsMetadata(OWSOperation operation, String service, String version) throws
            OwsExceptionReport;
}