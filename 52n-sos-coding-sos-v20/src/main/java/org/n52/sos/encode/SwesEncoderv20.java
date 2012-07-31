package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.swes.x20.DescribeSensorResponseDocument;
import net.opengis.swes.x20.DescribeSensorResponseType;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.AbstractServiceResponseObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwesEncoderv20 implements IEncoder<XmlObject, AbstractServiceResponseObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwesEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SwesEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SWEConstants.NS_SWES_20));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    // public XmlObject encode(Object response) throws OwsExceptionReport {
    //
    // }
    //
    // public XmlObject encode(Object response, Map<HelperValues, String>
    // additionalValues)
    // throws OwsExceptionReport {
    // Map<HelperValues, String> additionalValues = new HashMap<HelperValues,
    // String>();
    // additionalValues.put(HelperValues.VERSION, Sos2Constants.SERVICEVERSION);
    // return encode(response, additionalValues);
    //
    // }
    //
    // public void encode(XmlObject xmlObject, Object response,
    // Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
    // String operationName = additionalValues.get(HelperValues.OPERATION);
    // if (operationName.equals(SosConstants.Operations.DescribeSensor.name()))
    // {
    // return createDescribeSensorResponse((SosSensorML) response);
    // } else {
    // return null;
    // }
    // }

    @Override
    public XmlObject encode(AbstractServiceResponseObject response) throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
        additionalValues.put(HelperValues.VERSION, Sos2Constants.SERVICEVERSION);
        return encode(response, additionalValues);
    }

    @Override
    public XmlObject encode(AbstractServiceResponseObject response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        String operationName = additionalValues.get(HelperValues.OPERATION);
        if (operationName.equals(SosConstants.Operations.DescribeSensor.name()) && response instanceof SosSensorML) {
            return createDescribeSensorResponse((SosSensorML) response);
        } else {
            return null;
        }
    }

    private XmlObject createDescribeSensorResponse(SosSensorML sensorDesc) throws OwsExceptionReport {
        // DescribeSensorResponseDocument xbDescSensorRespDoc =
        // DescribeSensorResponseDocument.Factory.newInstance(SosXmlOptionsUtility.getInstance()
        // .getXmlOptions4Sos2Swe200());
        DescribeSensorResponseDocument xbDescSensorRespDoc =
                DescribeSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DescribeSensorResponseType descripeSensorResponse = xbDescSensorRespDoc.addNewDescribeSensorResponse();
        descripeSensorResponse.setProcedureDescriptionFormat(sensorDesc.getOutputFormat());
        String outputFormat = null;
        if (sensorDesc.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
            outputFormat = SensorMLConstants.NS_SML;
        } else {
            outputFormat = sensorDesc.getOutputFormat();
        }
        IEncoder encoder = Configurator.getInstance().getEncoder(outputFormat);
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(sensorDesc);
            if (xmlObject instanceof SensorMLDocument) {
                SensorMLDocument smlDoc = (SensorMLDocument) xmlObject;
                for (Member member : smlDoc.getSensorML().getMemberArray()) {
                    descripeSensorResponse.addNewDescription().addNewSensorDescription().addNewData()
                            .set(member.getProcess());
                }
                return xbDescSensorRespDoc;
            }
        }
        String exceptionText = "Error while encoding DescribeSensor response, missing encoder!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

}
