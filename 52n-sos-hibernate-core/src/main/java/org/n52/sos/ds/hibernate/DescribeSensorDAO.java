/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDescribeSensorDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SensorMLType;
import org.n52.sos.ogc.swe.SWEConstants.SosSensorDescription;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.SosDescribeSensorRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IDescribeSensorDAO
 * 
 */
public class DescribeSensorDAO implements IDescribeSensorDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DescribeSensorDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public DescribeSensorDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IDescribeSensorDAO#getSensorMLDescription(org.n52.sos.
     * request.AbstractSosRequest)
     */
    @Override
    public SosSensorML getSensorMLDescription(AbstractServiceRequest request) throws OwsExceptionReport {
        // sensorDocument which should be returned
        Session session = null;
        String filename = null;
        String descriptionType = null;
        String smlFile = null;
        try {
            SosDescribeSensorRequest sosRequest = (SosDescribeSensorRequest) request;
            session = (Session) connectionProvider.getConnection();
            Procedure procedure =
                    HibernateCriteriaQueryUtilities.getProcedureForIdentifier(sosRequest.getProcedure(), session);
            Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
            for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
                if (validProcedureTime.getEndTime() == null) {
                    filename = validProcedureTime.getDescriptionUrl();
                    smlFile = validProcedureTime.getDescriptionXml();
                }
            }
            descriptionType = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();

            String result = null;

            // check whether SMLFile or Url is set
            if (filename == null && smlFile == null) {
                String exceptionText =
                        "No sensorML file was found for the requested procedure " + sosRequest.getProcedure();
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);

            } else {

                if (filename != null && descriptionType != null && smlFile == null) {
                    // return sensorML from folder

                    if (!descriptionType.equalsIgnoreCase(sosRequest.getOutputFormat())
                            && !descriptionType.equalsIgnoreCase(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                        String exceptionText =
                                "The value of the output format is wrong and has to be " + descriptionType
                                        + " for procedure " + sosRequest.getProcedure();
                        LOGGER.error(exceptionText);
                        throw Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
                    }
                    FileInputStream fis = null;
                    try {

                        File sensorFile = null;
                        LOGGER.info(filename);

                        // read in the description file
                        if (filename.startsWith("standard/")) {
                            filename = filename.replace("standard/", "");
                            sensorFile = new File(Configurator.getInstance().getSensorDir(), filename);
                        } else {
                            sensorFile = new File(filename);
                        }

                        fis = new FileInputStream(sensorFile);

                        byte[] b = new byte[fis.available()];
                        fis.read(b);
                        fis.close();

                        // set result
                        result = new String(b);

                    } catch (FileNotFoundException fnfe) {
                        String exceptionText =
                                "No sensorML file was found for the requested procedure " + sosRequest.getProcedure();
                        LOGGER.error(exceptionText, fnfe);
                        throw Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
                    } catch (IOException ioe) {
                        String exceptionText = "An error occured while parsing the sensor description document!";
                        LOGGER.error(exceptionText, ioe);
                        throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException ioe) {
                                LOGGER.error("Error while closing the sensor description file input stream!", ioe);
                            }
                        }
                    }
                } else {
                    // return sensorML from DB
                    result = smlFile;
                }
            }
            return new SosSensorML(SosSensorDescription.XmlStringDescription, result,
                    ((SosDescribeSensorRequest) request).getOutputFormat());
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for DescribeSensor document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ISosOperationDAO#getOperationName()
     */
    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.ISosOperationDAO#getOperationsMetadata(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(OPERATION_NAME);
        // set param DCP
        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(SWEConstants.NS_SWES_20);
        }
        opsMeta.setDcp(SosHelper.getDCP(OPERATION_NAME, dkt, Configurator
                .getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set param procedure
        opsMeta.addParameterValue(SosConstants.GetObservationParams.procedure.name(), Configurator.getInstance()
                .getCapsCacheController().getProcedures());
        // set param output format
        List<String> outputFormatValues = new ArrayList<String>(1);
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            outputFormatValues.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
        }
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            outputFormatValues.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
        }
        opsMeta.addParameterValue(Sos1Constants.DescribeSensorParams.outputFormat.name(), outputFormatValues);
        return opsMeta;
    }

}
