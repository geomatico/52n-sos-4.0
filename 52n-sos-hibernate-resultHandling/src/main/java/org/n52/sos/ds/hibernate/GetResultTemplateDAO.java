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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetResultTemplateDAO;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.response.GetResultTemplateResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResultTemplateDAO implements IGetResultTemplateDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.GetResultTemplate.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;
    
    public GetResultTemplateDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        Session session = null;
        if (connection instanceof Session) {
            session = (Session) connection;
        } else {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        // get DCP
        DecoderKeyType dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        Map<String, List<String>> dcpMap =
                SosHelper.getDCP(OPERATION_NAME, dkt, Configurator.getInstance().getBindingOperators().values(),
                        Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
            // set DCP
            opsMeta.setDcp(dcpMap);

            // Get data from data source
            List<ResultTemplate> resultTemplates = HibernateCriteriaQueryUtilities.getResultTemplateObjects(session);
            Set<String> offerings = null;
            Set<String> observableProperties = null;
            if (resultTemplates != null && !resultTemplates.isEmpty()) {
                offerings = new HashSet<String>(0);
                observableProperties = new HashSet<String>(0);
                for (ResultTemplate resultTemplate : resultTemplates) {
                    ObservationConstellation observationConstellation = resultTemplate.getObservationConstellation();
                    offerings.add(observationConstellation.getOffering().getIdentifier());
                    observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
                }
            }
            // set param offering
            opsMeta.addParameterValue(Sos2Constants.GetResultTemplateParams.offering.name(),
                    new OWSParameterValuePossibleValues(offerings));
            // set param observedProperty
            opsMeta.addParameterValue(Sos2Constants.GetResultTemplateParams.observedProperty.name(),
                    new OWSParameterValuePossibleValues(observableProperties));
            return opsMeta;
        }
        return null;
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GetResultTemplateResponse getResultTemplate(GetResultTemplateRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            session = (Session) connectionProvider.getConnection();
            ResultTemplate resultRemplate =
                    HibernateCriteriaQueryUtilities.getResultTemplateObject(request.getOffering(),
                            request.getObservedProperty(), session);
            if (resultRemplate != null) {
                GetResultTemplateResponse response = new GetResultTemplateResponse();
                response.setService(request.getService());
                response.setVersion(request.getVersion());
                response.setResultEncoding(ResultHandlingHelper.createSosResultEncoding(resultRemplate.getResultEncoding()));
                response.setResultStructure(ResultHandlingHelper.createSosResultStructure(resultRemplate.getResultStructure()));
                return response;
            }
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("For the requested combination offering (");
            exceptionText.append(request.getOffering());
            exceptionText.append(") and observedProperty (");
            exceptionText.append(request.getObservedProperty());
            exceptionText.append(") no SWE Common 2.0 encoded result values are available!");
            throw Util4Exceptions.createInvalidPropertyOfferingCombination(exceptionText.toString());
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data result template data!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

}
