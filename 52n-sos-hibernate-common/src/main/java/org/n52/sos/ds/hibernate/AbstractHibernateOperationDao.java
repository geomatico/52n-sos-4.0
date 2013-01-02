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
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;

public abstract class AbstractHibernateOperationDao extends AbstractHibernateDao implements IOperationDAO {

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

    public abstract OWSOperation getOperationsMetadata(String service, String version, Session connection) throws OwsExceptionReport;

    protected Map<String, List<String>> getDCP(DecoderKeyType dkt) throws OwsExceptionReport {
        return SosHelper.getDCP(getOperationName(), dkt, getConfigurator().getBindingOperators().values(), getConfigurator().getServiceURL());
    }
}
