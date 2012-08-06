package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.opengis.sos.x20.SosInsertionMetadataPropertyType;
import net.opengis.sos.x20.SosInsertionMetadataType;
import net.opengis.swes.x20.DeleteSensorDocument;
import net.opengis.swes.x20.DescribeSensorDocument;
import net.opengis.swes.x20.DescribeSensorType;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.InsertSensorType;
import net.opengis.swes.x20.InsertSensorType.Metadata;
import net.opengis.swes.x20.UpdateSensorDescriptionDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionType;
import net.opengis.swes.x20.UpdateSensorDescriptionType.Description;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosMetadata;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        else if (xmlObject instanceof InsertSensorDocument) {
            InsertSensorDocument xbInsSensDoc = (InsertSensorDocument) xmlObject;
            response = parseInsertSensorRequest(xbInsSensDoc);
        }
        else if (xmlObject instanceof UpdateSensorDescriptionDocument) {
            UpdateSensorDescriptionDocument xbUpSenDoc = (UpdateSensorDescriptionDocument) xmlObject;
            response = parseUpdateSensorRequest(xbUpSenDoc);
        }
        else if (xmlObject instanceof DeleteSensorDocument) {
            DeleteSensorDocument xbDelSenDoc = (DeleteSensorDocument) xmlObject;
            response = parseDeleteSensorRequest(xbDelSenDoc);
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
    private AbstractServiceRequest parseDescribeSensor(DescribeSensorDocument xbDescSenDoc) throws OwsExceptionReport {
        // validate document
        XmlHelper.validateDocument(xbDescSenDoc);

        DescribeSensorRequest descSensorRequest = new DescribeSensorRequest();
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

    private AbstractServiceRequest parseInsertSensorRequest(InsertSensorDocument xbInsSensDoc)
            throws OwsExceptionReport {
        InsertSensorRequest request = new InsertSensorRequest();
        InsertSensorType xbInsertSensor = xbInsSensDoc.getInsertSensor();
        request.setSensorDescription(xbInsertSensor.getProcedureDescriptionFormat());
        if (xbInsertSensor.getObservablePropertyArray() != null
                && xbInsertSensor.getObservablePropertyArray().length > 0) {
            request.setObservableProperty(Arrays.asList(xbInsertSensor.getObservablePropertyArray()));
        }
        if (xbInsertSensor.getMetadataArray() != null && xbInsertSensor.getMetadataArray().length > 0) {
            request.setMetadata(parseMetadata(xbInsertSensor.getMetadataArray()));
        }
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(getNodeFromNodeList(xbInsertSensor.getProcedureDescription().getDomNode().getChildNodes()));
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return request;
    }

    /**
     * parses the Xmlbeans UpdateSensorDescription document to a SOS request.
     * 
     * @param xbUpSenDoc
     *            UpdateSensorDescription document
     * @return SOS UpdateSensor request
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private AbstractServiceRequest parseUpdateSensorRequest(UpdateSensorDescriptionDocument xbUpSenDoc)
            throws OwsExceptionReport {
        UpdateSensorRequest request = new UpdateSensorRequest();
        UpdateSensorDescriptionType xbUpdateSensor = xbUpSenDoc.getUpdateSensorDescription();
        request.setProcedureID(xbUpdateSensor.getProcedure());
        request.setSensorDescription(xbUpdateSensor.getProcedureDescriptionFormat());
        for (Description description : xbUpdateSensor.getDescriptionArray()) {
            // TODO: parse
            try {
                XmlObject xmlObject = XmlObject.Factory.parse(getNodeFromNodeList(description.getSensorDescription().getData().getDomNode().getChildNodes()));
            } catch (XmlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return request;
    }

    private AbstractServiceRequest parseDeleteSensorRequest(DeleteSensorDocument xbDelSenDoc) {
        return new DeleteSensorRequest(xbDelSenDoc.getDeleteSensor().getProcedure());
    }

    private SosMetadata parseMetadata(Metadata[] metadataArray) throws OwsExceptionReport {

        SosMetadata sosMetadata = new SosMetadata();
        try {
            for (Metadata metadata : metadataArray) {
//                SosInsertionMetadataPropertyType sos = (SosInsertionMetadataPropertyType)metadata.getInsertionMetadata();
                if (metadata.getDomNode().hasChildNodes()) {
                    Node node = getNodeFromNodeList(metadata.getDomNode().getChildNodes());
                    SosInsertionMetadataPropertyType xbMetadata = SosInsertionMetadataPropertyType.Factory.parse(node);
                    SosInsertionMetadataType xbSosInsertionMetadata = xbMetadata.getSosInsertionMetadata();
                    if (xbSosInsertionMetadata.getFeatureOfInterestTypeArray() != null
                            && xbSosInsertionMetadata.getFeatureOfInterestTypeArray().length > 0) {
                        sosMetadata.setFeatureOfInterestTypes(Arrays.asList(xbSosInsertionMetadata
                                .getFeatureOfInterestTypeArray()));
                    }
                    if (xbSosInsertionMetadata.getObservationTypeArray() != null
                            && xbSosInsertionMetadata.getObservationTypeArray().length > 0) {
                        sosMetadata
                                .setObservationTypes(Arrays.asList(xbSosInsertionMetadata.getObservationTypeArray()));
                    }
                }
            }
        } catch (XmlException xmle) {
            String exceptionText = "An error occurred while parsing the metadata in the http post request";
            LOGGER.error(exceptionText, xmle);
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
        }
        return sosMetadata;
    }
    
    private static Node getNodeFromNodeList(NodeList nodeList) {
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    return nodeList.item(i);
                }
            }
        }
        return null;
    }

}
