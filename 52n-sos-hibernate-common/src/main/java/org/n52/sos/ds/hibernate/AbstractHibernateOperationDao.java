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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.n52.sos.binding.Binding;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHibernateOperationDao extends AbstractHibernateDao implements IOperationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHibernateOperationDao.class);

    @Override
    public final OWSOperation getOperationsMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        return getOperationsMetadata(service, version, getSession(connection));
    }

    @Override
    public final IExtension getExtension(Object connection) throws OwsExceptionReport {
        return getExtension(getSession(connection));
    }

    protected ACapabilitiesCacheController getCache() {
        return Configurator.getInstance().getCapabilitiesCacheController();
    }

    protected Configurator getConfigurator() {
        return Configurator.getInstance();
    }

    /* provide a default implemenation for extension-less DAO's */
    protected IExtension getExtension(Session session) throws OwsExceptionReport {
        return null;
    }

    protected OWSOperation getOperationsMetadata(String service, String version, Session session) throws OwsExceptionReport {

        Map<String, List<String>> dcp =  getDCP(new OperationDecoderKey(service, version, getOperationName()));
        if (dcp == null || dcp.isEmpty()) {
            LOGGER.debug("Operation {} not available due to empty DCP map.", getOperationName());
            return null;
        }
        OWSOperation operation = new OWSOperation();
        operation.setDcp(dcp);
        operation.setOperationName(getOperationName());
        setOperationsMetadata(operation, service, version, session);
        return operation;
    }

	/**
     * Get the HTTP DCPs for a operation
     *
     * @param decoderKey the decoderKey
     * @return Map with DCPs for the SOS operation
     * @throws OwsExceptionReport
     */
    protected Map<String, List<String>> getDCP(OperationDecoderKey decoderKey) throws OwsExceptionReport {
        List<String> httpGetUrls = new LinkedList<String>();
        List<String> httpPostUrls = new LinkedList<String>();
        List<String> httpPutUrls = new LinkedList<String>();
        List<String> httpDeleteUrls = new LinkedList<String>();
        String serviceURL = Configurator.getInstance().getServiceURL();
        try {
            for (Binding binding : Configurator.getInstance().getBindingRepository().getBindings().values()) {
                // HTTP-Get
                if (binding.checkOperationHttpGetSupported( decoderKey)) {
                    httpGetUrls.add(serviceURL + binding.getUrlPattern() + "?");
                }
                // HTTP-Post
                if (binding.checkOperationHttpPostSupported(decoderKey)) {
                    httpPostUrls.add(serviceURL + binding.getUrlPattern());
                }
                // HTTP-PUT
                if (binding.checkOperationHttpPutSupported(decoderKey)) {
                    httpPutUrls.add(serviceURL + binding.getUrlPattern());
                }
                // HTTP-DELETE
                if (binding.checkOperationHttpDeleteSupported(decoderKey)) {
                    httpDeleteUrls.add(serviceURL + binding.getUrlPattern());
                }

            }
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            }
            // FIXME valid exception
            OwsExceptionReport owse = new OwsExceptionReport();
            // owse.addCodedException(invalidparametervalue, locator, message,
            // e);
            throw owse;
        }

        Map<String, List<String>> dcp = new HashMap<String, List<String>>(4);
        if (!httpGetUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_GET, httpGetUrls);
        }
        if (!httpPostUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_POST, httpPostUrls);
        }
        if (!httpPutUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_PUT, httpPutUrls);
        }
        if (!httpDeleteUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_DELETE, httpDeleteUrls);
        }
        return dcp;
    }

    protected abstract void setOperationsMetadata(OWSOperation operation, String service, String version, Session session) throws OwsExceptionReport;
}