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
package org.n52.sos.ds;

import java.util.Map;
import java.util.Set;

import org.n52.sos.binding.Binding;
import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.DCP;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.util.MultiMaps;
import org.n52.sos.util.SetMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationDAO implements OperationDAO {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOperationDAO.class);
    private final OperationDAOKeyType operationDAOIdentifier;

    public AbstractOperationDAO(String service, String operationName) {
        operationDAOIdentifier = new OperationDAOKeyType(service, operationName);
    }

    //     TODO check if necessary in feature
    @Override
    public String getOperationName() {
        return this.operationDAOIdentifier.getOperationName();
    }
    
    @Override
    public OperationDAOKeyType getOperationDAOKeyType() {
        return this.operationDAOIdentifier;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version) throws OwsExceptionReport {
        Map<String, Set<DCP>> dcp = getDCP(new OperationDecoderKey(service, version, getOperationDAOKeyType().getOperationName()));
        if (dcp == null || dcp.isEmpty()) {
            LOG.debug("Operation {} for Service {} not available due to empty DCP map.", getOperationName(), getOperationDAOKeyType().getService());
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
    protected Map<String, Set<DCP>> getDCP(OperationDecoderKey decoderKey) throws OwsExceptionReport {
        SetMultiMap<String, DCP> dcps = MultiMaps.newSetMultiMap();
        String serviceURL = ServiceConfiguration.getInstance().getServiceURL();
        try {
            for (Binding binding : BindingRepository.getInstance().getBindings().values()) {
                String url = serviceURL + binding.getUrlPattern();
                if (binding.checkOperationHttpGetSupported(decoderKey)) {
                    dcps.add(SosConstants.HTTP_GET, new DCP(binding.getEncoding(), url + "?"));
                }
                if (binding.checkOperationHttpPostSupported(decoderKey)) {
                    dcps.add(SosConstants.HTTP_POST, new DCP(binding.getEncoding(), url));
                }
                if (binding.checkOperationHttpPutSupported(decoderKey)) {
                    dcps.add(SosConstants.HTTP_PUT, new DCP(binding.getEncoding(), url));
                }
                if (binding.checkOperationHttpDeleteSupported(decoderKey)) {
                    dcps.add(SosConstants.HTTP_DELETE, new DCP(binding.getEncoding(), url));
                }
            }
        } catch (Exception e) {
            // FIXME valid exception
            throw new NoApplicableCodeException().causedBy(e);
        }

        return dcps;
    }

    protected abstract void setOperationsMetadata(OWSOperation operation, String service, String version) throws
            OwsExceptionReport;
}
