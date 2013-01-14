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
package org.n52.sos.ds.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
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
       
        Map<String, List<String>> dcp =  SosHelper.getDCP(new OperationDecoderKey(service, version, getOperationName()));
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

    protected abstract void setOperationsMetadata(OWSOperation operation, String service, String version, Session session) throws OwsExceptionReport;
}