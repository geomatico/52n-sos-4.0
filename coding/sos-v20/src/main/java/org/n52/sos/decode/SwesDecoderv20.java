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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.sos.x20.SosInsertionMetadataPropertyType;
import net.opengis.sos.x20.SosInsertionMetadataType;
import net.opengis.swes.x20.DeleteSensorDocument;
import net.opengis.swes.x20.DescribeSensorDocument;
import net.opengis.swes.x20.DescribeSensorType;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.InsertSensorType;
import net.opengis.swes.x20.InsertSensorType.Metadata;
import net.opengis.swes.x20.InsertSensorType.RelatedFeature;
import net.opengis.swes.x20.UpdateSensorDescriptionDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionType;
import net.opengis.swes.x20.UpdateSensorDescriptionType.Description;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.Sos2Constants.UpdateSensorDescriptionParams;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosInsertionMetadata;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swes.SwesFeatureRelationship;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SwesDecoderv20 implements Decoder<AbstractServiceCommunicationObject, XmlObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwesDecoderv20.class);

    @SuppressWarnings("unchecked")
    private static final Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(CodingHelper.decoderKeysForElements(
            SWEConstants.NS_SWES_20, DescribeSensorDocument.class, InsertSensorDocument.class,
            UpdateSensorDescriptionDocument.class, DeleteSensorDocument.class), CodingHelper
            .xmlDecoderKeysForOperation(SosConstants.SOS, Sos2Constants.SERVICEVERSION,
                    SosConstants.Operations.DescribeSensor, Sos2Constants.Operations.InsertSensor,
                    Sos2Constants.Operations.UpdateSensorDescription, Sos2Constants.Operations.DeleteSensor));

    public SwesDecoderv20() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public AbstractServiceRequest decode(final XmlObject xmlObject) throws OwsExceptionReport {
        LOGGER.debug("REQUESTTYPE:" + xmlObject.getClass());
        XmlHelper.validateDocument(xmlObject);
        if (xmlObject instanceof DescribeSensorDocument) {
            return parseDescribeSensor((DescribeSensorDocument) xmlObject);
        } else if (xmlObject instanceof InsertSensorDocument) {
            return parseInsertSensor((InsertSensorDocument) xmlObject);
        } else if (xmlObject instanceof UpdateSensorDescriptionDocument) {
            return parseUpdateSensorDescription((UpdateSensorDescriptionDocument) xmlObject);
        } else if (xmlObject instanceof DeleteSensorDocument) {
            return parseDeleteSensor((DeleteSensorDocument) xmlObject);
        } else {
            throw new UnsupportedDecoderInputException(this, xmlObject);
        }
    }

    /**
     * parses the passes XmlBeans document and creates a SOS describeSensor
     * request
     * 
     * @param xbDescSenDoc
     *            XmlBeans document representing the describeSensor request
     * @return Returns SOS describeSensor request
     * 
     * 
     * @throws OwsExceptionReport
     *             * if validation of the request failed
     */
    private AbstractServiceRequest parseDescribeSensor(final DescribeSensorDocument xbDescSenDoc) throws OwsExceptionReport {
        final DescribeSensorRequest descSensorRequest = new DescribeSensorRequest();
        final DescribeSensorType xbDescSensor = xbDescSenDoc.getDescribeSensor();
        descSensorRequest.setService(xbDescSensor.getService());
        descSensorRequest.setVersion(xbDescSensor.getVersion());
        descSensorRequest.setProcedures(xbDescSensor.getProcedure());
        if (xbDescSensor.getProcedureDescriptionFormat() != null
                && !xbDescSensor.getProcedureDescriptionFormat().isEmpty()) {
            descSensorRequest.setProcedureDescriptionFormat(xbDescSensor.getProcedureDescriptionFormat());
        } else {
            descSensorRequest.setProcedureDescriptionFormat(SosConstants.PARAMETER_NOT_SET);
        }
        return descSensorRequest;
    }

    private AbstractServiceRequest parseInsertSensor(final InsertSensorDocument xbInsSensDoc) throws OwsExceptionReport {
        final InsertSensorRequest request = new InsertSensorRequest();
        final InsertSensorType xbInsertSensor = xbInsSensDoc.getInsertSensor();
        request.setService(xbInsertSensor.getService());
        request.setVersion(xbInsertSensor.getVersion());
        // format
        request.setProcedureDescriptionFormat(xbInsertSensor.getProcedureDescriptionFormat());
        // observable properties
        if (xbInsertSensor.getObservablePropertyArray() != null
                && xbInsertSensor.getObservablePropertyArray().length > 0) {
            request.setObservableProperty(Arrays.asList(xbInsertSensor.getObservablePropertyArray()));
        }
        // related features
        if (xbInsertSensor.getRelatedFeatureArray() != null 
        		&& xbInsertSensor.getRelatedFeatureArray().length > 0) {
        	request.setRelatedFeature(parseRelatedFeature(xbInsertSensor.getRelatedFeatureArray()));
        }
        // metadata
        if (xbInsertSensor.getMetadataArray() != null && xbInsertSensor.getMetadataArray().length > 0) {
            request.setMetadata(parseMetadata(xbInsertSensor.getMetadataArray()));
        }
        SosHelper.checkProcedureDescriptionFormat(xbInsertSensor.getProcedureDescriptionFormat(),
                Sos2Constants.InsertSensorParams.procedureDescriptionFormat.name());

        try {
            final XmlObject xbProcedureDescription =
                    XmlObject.Factory.parse(getNodeFromNodeList(xbInsertSensor.getProcedureDescription().getDomNode()
                            .getChildNodes()));

            final Decoder<?, XmlObject> decoder = CodingRepository.getInstance().getDecoder(
                    new XmlNamespaceDecoderKey(xbInsertSensor.getProcedureDescriptionFormat(), xbProcedureDescription.getClass()));
            if (decoder == null) {
                throw new InvalidParameterValueException().at(
                        Sos2Constants.InsertSensorParams.procedureDescriptionFormat)
                        .withMessage("The requested %s is not supported!",
                        Sos2Constants.InsertSensorParams.procedureDescriptionFormat.name());
            }
            final Object decodedProcedureDescription = decoder.decode(xbProcedureDescription);
            if (decodedProcedureDescription instanceof SosProcedureDescription) {
                request.setProcedureDescription((SosProcedureDescription) decodedProcedureDescription);
            }
        } catch (final XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle)
                    .withMessage("Error while parsing procedure description of InsertSensor request!");
        }
        return request;
    }

    private AbstractServiceRequest parseDeleteSensor(final DeleteSensorDocument xbDelSenDoc) {
        final DeleteSensorRequest request = new DeleteSensorRequest();
        request.setService(xbDelSenDoc.getDeleteSensor().getService());
        request.setVersion(xbDelSenDoc.getDeleteSensor().getVersion());
        request.setProcedureIdentifier(xbDelSenDoc.getDeleteSensor().getProcedure());
        return request;
    }

    /**
     * parses the Xmlbeans UpdateSensorDescription document to a SOS request.
     * 
     * @param xbUpSenDoc
     *            UpdateSensorDescription document
     * @return SOS UpdateSensor request
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs.
     */
    private AbstractServiceRequest parseUpdateSensorDescription(final UpdateSensorDescriptionDocument xbUpSenDoc)
            throws OwsExceptionReport {
        final UpdateSensorRequest request = new UpdateSensorRequest();
        final UpdateSensorDescriptionType xbUpdateSensor = xbUpSenDoc.getUpdateSensorDescription();
        request.setService(xbUpdateSensor.getService());
        request.setVersion(xbUpdateSensor.getVersion());
        request.setProcedureIdentifier(xbUpdateSensor.getProcedure());
        request.setProcedureDescriptionFormat(xbUpdateSensor.getProcedureDescriptionFormat());

        for (final Description description : xbUpdateSensor.getDescriptionArray()) {
            try {
                final XmlObject xmlObject =
                        XmlObject.Factory.parse(getNodeFromNodeList(description.getSensorDescription().getData()
                                .getDomNode().getChildNodes()));
                final Decoder<?, XmlObject> decoder =
                        CodingRepository.getInstance().getDecoder(CodingHelper.getDecoderKey(xmlObject));
                if (decoder == null) {
                    throw new InvalidParameterValueException().at(
                            UpdateSensorDescriptionParams.procedureDescriptionFormat).withMessage(
                            "The requested procedureDescritpionFormat is not supported!");
                }

                final Object decodedObject = decoder.decode(xmlObject);
                if (decodedObject instanceof SosProcedureDescription) {
                    request.addProcedureDescriptionString((SosProcedureDescription) decodedObject);
                }
            } catch (final XmlException xmle) {
                throw new NoApplicableCodeException().causedBy(xmle).withMessage(
                        "Error while parsing procedure description of UpdateSensor request!");
            }
        }
        return request;
    }

    private SosInsertionMetadata parseMetadata(final Metadata[] metadataArray) throws OwsExceptionReport {

        final SosInsertionMetadata sosMetadata = new SosInsertionMetadata();
        try {
            for (final Metadata metadata : metadataArray) {
                SosInsertionMetadataType xbSosInsertionMetadata = null;
                if (metadata.getInsertionMetadata() != null
                        && metadata.getInsertionMetadata().schemaType() == SosInsertionMetadataType.type) {
                    xbSosInsertionMetadata = (SosInsertionMetadataType) metadata.getInsertionMetadata();
                } else {
                    if (metadata.getDomNode().hasChildNodes()) {
                        final Node node = getNodeFromNodeList(metadata.getDomNode().getChildNodes());
                        final SosInsertionMetadataPropertyType xbMetadata =
                                SosInsertionMetadataPropertyType.Factory.parse(node);
                        xbSosInsertionMetadata = xbMetadata.getSosInsertionMetadata();
                    }
                }
                if (xbSosInsertionMetadata != null) {
                    // featureOfInterest types
                    if (xbSosInsertionMetadata.getFeatureOfInterestTypeArray() != null) {
                        sosMetadata.setFeatureOfInterestTypes(Arrays.asList(xbSosInsertionMetadata
                                .getFeatureOfInterestTypeArray()));
                    }
                    // observation types
                    if (xbSosInsertionMetadata.getObservationTypeArray() != null) {
                        sosMetadata
                                .setObservationTypes(Arrays.asList(xbSosInsertionMetadata.getObservationTypeArray()));
                    }
                }
            }
        } catch (final XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle).withMessage(
                    "An error occurred while parsing the metadata in the http post request");
        }
        return sosMetadata;
    }

    private List<SwesFeatureRelationship> parseRelatedFeature(final RelatedFeature[] relatedFeatureArray) {
        final List<SwesFeatureRelationship> sosRelatedFeatures =
                new ArrayList<SwesFeatureRelationship>(relatedFeatureArray.length);
        for (final RelatedFeature relatedFeature : relatedFeatureArray) {
            final SwesFeatureRelationship sosFeatureRelationship = new SwesFeatureRelationship();

            final FeaturePropertyType fpt = relatedFeature.getFeatureRelationship().getTarget();
            if (fpt.getHref() != null && !fpt.getHref().isEmpty()) {
                final String identifier = fpt.getHref();
                final SamplingFeature feature = new SamplingFeature(new CodeWithAuthority(identifier));
                if (fpt.getTitle() != null && !fpt.getTitle().isEmpty()) {
                   feature.setName(CollectionHelper.asList(new CodeType(fpt.getTitle())));
                }
                if (checkForRequestUrl(fpt.getHref())) {
                    feature.setUrl(fpt.getHref());
                }
                feature.setFeatureType(OGCConstants.UNKNOWN);
                sosFeatureRelationship.setFeature(feature);
//            } else {
                // TODO: decode encoded feature, XML-Represent
            }
            sosFeatureRelationship.setRole(relatedFeature.getFeatureRelationship().getRole());
            sosRelatedFeatures.add(sosFeatureRelationship);
        }
        return sosRelatedFeatures;
    }

    private boolean checkForRequestUrl(final String href) {
        return href.toLowerCase().contains("request=");
    }

    private Node getNodeFromNodeList(final NodeList nodeList) {
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
