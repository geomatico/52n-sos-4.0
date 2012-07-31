package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.List;

import net.opengis.swes.x20.DeleteSensorDocument;
import net.opengis.swes.x20.DescribeSensorDocument;
import net.opengis.swes.x20.DescribeSensorType;
import net.opengis.swes.x20.InsertSensorDocument;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.SosDeleteSensorRequest;
import org.n52.sos.request.SosDescribeSensorRequest;
import org.n52.sos.request.SosInsertSensorRequest;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwesDecoderv20 implements IXmlRequestDecoder {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwesDecoderv20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SwesDecoderv20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        DecoderKeyType namespaceDKT = new DecoderKeyType(SWEConstants.NS_SWES_20);
        decoderKeyTypes.add(namespaceDKT);
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public AbstractServiceRequest decode(XmlObject xmlObject) throws OwsExceptionReport {
        AbstractServiceRequest response = null;
        LOGGER.debug("REQUESTTYPE:" + xmlObject.getClass());
        
        if (xmlObject instanceof DescribeSensorDocument) {
            DescribeSensorDocument obsDoc = (DescribeSensorDocument) xmlObject;
            response = parseDescribeSensor(obsDoc);
        }
        return response;
    }
    
    /**
     * parses the passes XmlBeans document and creates a SOS describeSensor
     * request
     * 
     * @param xbDescSenDoc
     *            XmlBeans document representing the describeSensor request
     * @return Returns SOS describeSensor request
     * @throws OwsExceptionReport
     *             if validation of the request failed
     */
    private AbstractServiceRequest parseDescribeSensor(DescribeSensorDocument xbDescSenDoc)
            throws OwsExceptionReport {
        // validate document
        XmlHelper.validateDocument(xbDescSenDoc);

        SosDescribeSensorRequest descSensorRequest = new SosDescribeSensorRequest();
        DescribeSensorType xbDescSensor = xbDescSenDoc.getDescribeSensor();
        descSensorRequest.setService(xbDescSensor.getService());
        descSensorRequest.setVersion(xbDescSensor.getVersion());
        descSensorRequest.setProcedures(xbDescSensor.getProcedure());
        if (xbDescSensor.getProcedureDescriptionFormat() != null
                && !xbDescSensor.getProcedureDescriptionFormat().equals("")) {
            descSensorRequest.setOutputFormat(xbDescSensor.getProcedureDescriptionFormat());
        } else {
            descSensorRequest.setOutputFormat(SosConstants.PARAMETER_NOT_SET);
        }
        return descSensorRequest;
    }
    
    private AbstractServiceRequest parseInsertSensor(InsertSensorDocument xbInsertSensorDoc) throws OwsExceptionReport {
        // validate document
        XmlHelper.validateDocument(xbInsertSensorDoc);
        
        SosInsertSensorRequest insertSensorRequest = new SosInsertSensorRequest();
        
        return insertSensorRequest;
    }
    
    private AbstractServiceRequest parseDeleteSensor(DeleteSensorDocument xbDeleteSensor) throws OwsExceptionReport {
     // validate document
        XmlHelper.validateDocument(xbDeleteSensor);
        
        SosDeleteSensorRequest deleteSensorRequest = new SosDeleteSensorRequest();
        
        return deleteSensorRequest;
    }

}
