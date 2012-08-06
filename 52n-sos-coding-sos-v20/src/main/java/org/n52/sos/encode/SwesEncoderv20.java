package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.opengis.swes.x20.DescribeSensorResponseDocument;
import net.opengis.swes.x20.DescribeSensorResponseType;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwesEncoderv20 implements IEncoder<XmlObject, AbstractServiceResponse> {

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

    @Override
    public XmlObject encode(AbstractServiceResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(AbstractServiceResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response instanceof DescribeSensorResponse) {
            return createDescribeSensorResponse((DescribeSensorResponse) response);
        }
        return null;
    }

    private XmlObject createDescribeSensorResponse(DescribeSensorResponse response) throws OwsExceptionReport {
        DescribeSensorResponseDocument xbDescSensorRespDoc =
                DescribeSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DescribeSensorResponseType describeSensorResponse = xbDescSensorRespDoc.addNewDescribeSensorResponse();
        describeSensorResponse.setProcedureDescriptionFormat(response.getOutputFormat());
        String outputFormat = null;
        if (response.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
            outputFormat = SensorMLConstants.NS_SML;
        } else {
            outputFormat = response.getOutputFormat();
        }
        IEncoder encoder = Configurator.getInstance().getEncoder(outputFormat);
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(response.getSensorDescription());
            describeSensorResponse.addNewDescription().addNewSensorDescription().addNewData().set(xmlObject);
            return xbDescSensorRespDoc;
        }
        String exceptionText = "Error while encoding DescribeSensor response, missing encoder!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

}
