package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.ComparisonOperatorsType;
import net.opengis.fes.x20.ConformanceType;
import net.opengis.fes.x20.FilterCapabilitiesDocument.FilterCapabilities;
import net.opengis.fes.x20.GeometryOperandsType;
import net.opengis.fes.x20.IdCapabilitiesType;
import net.opengis.fes.x20.ScalarCapabilitiesType;
import net.opengis.fes.x20.SpatialCapabilitiesType;
import net.opengis.fes.x20.SpatialOperatorType;
import net.opengis.fes.x20.SpatialOperatorsType;
import net.opengis.fes.x20.TemporalCapabilitiesType;
import net.opengis.fes.x20.TemporalOperandsType;
import net.opengis.fes.x20.TemporalOperatorType;
import net.opengis.fes.x20.TemporalOperatorsType;
import net.opengis.fes.x20.impl.ComparisonOperatorNameTypeImpl;
import net.opengis.fes.x20.impl.SpatialOperatorNameTypeImpl;
import net.opengis.fes.x20.impl.TemporalOperatorNameTypeImpl;
import net.opengis.ows.x11.DomainType;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.ConformanceClassConstraintNames;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FesEncoderv20 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FesEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public FesEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(FilterConstants.NS_FES_2));
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
    public XmlObject encode(Object element) throws OwsExceptionReport {
        if (element instanceof org.n52.sos.ogc.filter.FilterCapabilities) {
           return encodeFilterCapabilities((org.n52.sos.ogc.filter.FilterCapabilities)element);
        }
        return null;
    }
    
    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Sets the filter capabilities section to capabilities
     * 
     * @param filterCapabilities
     *            FES filter capabilities
     * @param sosFilterCaps
     *            SOS filter capabilities
     */
    private XmlObject encodeFilterCapabilities(
                    org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {
        FilterCapabilities filterCapabilities = FilterCapabilities.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            setConformance(filterCapabilities.addNewConformance());
            if (sosFilterCaps.getComparisonOperators() != null
                            && !sosFilterCaps.getComparisonOperators().isEmpty()) {
                    setScalarFilterCapabilities(
                                    filterCapabilities.addNewScalarCapabilities(),
                                    sosFilterCaps);
            }
            if (sosFilterCaps.getSpatialOperands() != null
                            && !sosFilterCaps.getSpatialOperands().isEmpty()) {
                    setSpatialFilterCapabilities(
                                    filterCapabilities.addNewSpatialCapabilities(),
                                    sosFilterCaps);
            }
            if (sosFilterCaps.getTemporalOperands() != null
                            && !sosFilterCaps.getTemporalOperands().isEmpty()) {
                    setTemporalFilterCapabilities(
                                    filterCapabilities.addNewTemporalCapabilities(),
                                    sosFilterCaps);
            }
            // setIdFilterCapabilities(filterCapabilities.addNewIdCapabilities());
            return filterCapabilities;

    }
    
    /**
     * Sets the FES conformance classes in the filter capabilities section.
     * 
     * @param conformance
     *            FES conformence
     */
    private void setConformance(ConformanceType conformance) {
            // set Query conformance class
            DomainType implQuery = conformance.addNewConstraint();
            implQuery.setName(ConformanceClassConstraintNames.ImplementsQuery
                            .name());
            implQuery.addNewNoValues();
            implQuery.addNewDefaultValue().setStringValue("false");
            // set Ad hoc query conformance class
            DomainType implAdHocQuery = conformance.addNewConstraint();
            implAdHocQuery
                            .setName(ConformanceClassConstraintNames.ImplementsAdHocQuery
                                            .name());
            implAdHocQuery.addNewNoValues();
            implAdHocQuery.addNewDefaultValue().setStringValue("false");
            // set Functions conformance class
            DomainType implFunctions = conformance.addNewConstraint();
            implFunctions
                            .setName(ConformanceClassConstraintNames.ImplementsFunctions
                                            .name());
            implFunctions.addNewNoValues();
            implFunctions.addNewDefaultValue().setStringValue("false");
            // set Resource Identification conformance class
            DomainType implResourceId = conformance.addNewConstraint();
            implResourceId
                            .setName(ConformanceClassConstraintNames.ImplementsResourceld
                                            .name());
            implResourceId.addNewNoValues();
            implResourceId.addNewDefaultValue().setStringValue("false");
            // set Minimum Standard Filter conformance class
            DomainType implMinStandardFilter = conformance.addNewConstraint();
            implMinStandardFilter
                            .setName(ConformanceClassConstraintNames.ImplementsMinStandardFilter
                                            .name());
            implMinStandardFilter.addNewNoValues();
            implMinStandardFilter.addNewDefaultValue().setStringValue("false");
            // set Standard Filter conformance class
            DomainType implStandardFilter = conformance.addNewConstraint();
            implStandardFilter
                            .setName(ConformanceClassConstraintNames.ImplementsStandardFilter
                                            .name());
            implStandardFilter.addNewNoValues();
            implStandardFilter.addNewDefaultValue().setStringValue("false");
            // set Minimum Spatial Filter conformance class
            DomainType implMinSpatialFilter = conformance.addNewConstraint();
            implMinSpatialFilter
                            .setName(ConformanceClassConstraintNames.ImplementsMinSpatialFilter
                                            .name());
            implMinSpatialFilter.addNewNoValues();
            implMinSpatialFilter.addNewDefaultValue().setStringValue("true");
            // set Spatial Filter conformance class
            DomainType implSpatialFilter = conformance.addNewConstraint();
            implSpatialFilter
                            .setName(ConformanceClassConstraintNames.ImplementsSpatialFilter
                                            .name());
            implSpatialFilter.addNewNoValues();
            implSpatialFilter.addNewDefaultValue().setStringValue("true");
            // set Minimum Temporal Filter conformance class
            DomainType implMinTemporalFilter = conformance.addNewConstraint();
            implMinTemporalFilter
                            .setName(ConformanceClassConstraintNames.ImplementsMinTemporalFilter
                                            .name());
            implMinTemporalFilter.addNewNoValues();
            implMinTemporalFilter.addNewDefaultValue().setStringValue("true");
            // set Temporal Filter conformance class
            DomainType implTemporalFilter = conformance.addNewConstraint();
            implTemporalFilter
                            .setName(ConformanceClassConstraintNames.ImplementsTemporalFilter
                                            .name());
            implTemporalFilter.addNewNoValues();
            implTemporalFilter.addNewDefaultValue().setStringValue("true");
            // set Version navigation conformance class
            DomainType implVersionNav = conformance.addNewConstraint();
            implVersionNav
                            .setName(ConformanceClassConstraintNames.ImplementsVersionNav
                                            .name());
            implVersionNav.addNewNoValues();
            implVersionNav.addNewDefaultValue().setStringValue("false");
            // set Sorting conformance class
            DomainType implSorting = conformance.addNewConstraint();
            implSorting.setName(ConformanceClassConstraintNames.ImplementsSorting
                            .name());
            implSorting.addNewNoValues();
            implSorting.addNewDefaultValue().setStringValue("false");
            // set Extended Operators conformance class
            DomainType implExtendedOperators = conformance.addNewConstraint();
            implExtendedOperators
                            .setName(ConformanceClassConstraintNames.ImplementsExtendedOperators
                                            .name());
            implExtendedOperators.addNewNoValues();
            implExtendedOperators.addNewDefaultValue().setStringValue("false");
            // set Minimum XPath conformance class
            DomainType implMinimumXPath = conformance.addNewConstraint();
            implMinimumXPath
                            .setName(ConformanceClassConstraintNames.ImplementsMinimumXPath
                                            .name());
            implMinimumXPath.addNewNoValues();
            implMinimumXPath.addNewDefaultValue().setStringValue("false");
            // set Schema Element Function conformance class
            DomainType implSchemaElementFunc = conformance.addNewConstraint();
            implSchemaElementFunc
                            .setName(ConformanceClassConstraintNames.ImplementsSchemaElementFunc
                                            .name());
            implSchemaElementFunc.addNewNoValues();
            implSchemaElementFunc.addNewDefaultValue().setStringValue("false");

    }
    
    /**
     * Sets the SpatialFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param spatialCapabilitiesType
     *            FES SpatialCapabilities.
     * @param sosFilterCaps
     *            SOS spatial filter information
     */
    private void setSpatialFilterCapabilities(
                    SpatialCapabilitiesType spatialCapabilitiesType,
                    org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

            // set GeometryOperands
            if (sosFilterCaps.getSpatialOperands() != null
                            && !sosFilterCaps.getSpatialOperands().isEmpty()) {
                    GeometryOperandsType spatialOperands = spatialCapabilitiesType
                                    .addNewGeometryOperands();
                    for (QName operand : sosFilterCaps.getSpatialOperands()) {
                            spatialOperands.addNewGeometryOperand().setName(operand);
                    }
            }

            // set SpatialOperators
            if (sosFilterCaps.getSpatialOperators() != null
                            && !sosFilterCaps.getSpatialOperators().isEmpty()) {
                    SpatialOperatorsType spatialOps = spatialCapabilitiesType
                                    .addNewSpatialOperators();
                    Set<SpatialOperator> keys = sosFilterCaps.getSpatialOperators()
                                    .keySet();
                    for (SpatialOperator spatialOperator : keys) {
                            SpatialOperatorType operator = spatialOps
                                            .addNewSpatialOperator();
                            operator.setName(getEnum4SpatialOperator(spatialOperator));
                            GeometryOperandsType geomOps = operator
                                            .addNewGeometryOperands();
                            for (QName operand : sosFilterCaps.getSpatialOperators().get(
                                            spatialOperator)) {
                                    geomOps.addNewGeometryOperand().setName(operand);
                            }
                    }
            }
    }

    /**
     * Sets the TemporalFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param temporalCapabilitiesType
     *            FES TemporalCapabilities.
     * @param sosFilterCaps
     *            SOS temporal filter information
     */
    private void setTemporalFilterCapabilities(
                    TemporalCapabilitiesType temporalCapabilitiesType,
                    org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

            // set TemporalOperands
            if (sosFilterCaps.getTemporalOperands() != null
                            && !sosFilterCaps.getTemporalOperands().isEmpty()) {
                    TemporalOperandsType tempOperands = temporalCapabilitiesType
                                    .addNewTemporalOperands();
                    for (QName operand : sosFilterCaps.getTemporalOperands()) {
                            tempOperands.addNewTemporalOperand().setName(operand);
                    }
            }

            // set TemporalOperators
            if (sosFilterCaps.getTempporalOperators() != null
                            && !sosFilterCaps.getTempporalOperators().isEmpty()) {
                    TemporalOperatorsType temporalOps = temporalCapabilitiesType
                                    .addNewTemporalOperators();
                    Set<TimeOperator> keys = sosFilterCaps.getTempporalOperators()
                                    .keySet();
                    for (TimeOperator temporalOperator : keys) {
                            TemporalOperatorType operator = temporalOps
                                            .addNewTemporalOperator();
                            operator.setName(getEnum4TemporalOperator(temporalOperator));
                            TemporalOperandsType bboxGeomOps = operator
                                            .addNewTemporalOperands();
                            for (QName operand : sosFilterCaps.getTempporalOperators().get(
                                            temporalOperator)) {
                                    bboxGeomOps.addNewTemporalOperand().setName(operand);
                            }
                    }
            }
    }

    /**
     * Sets the ScalarFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param scalarCapabilitiesType
     *            FES ScalarCapabilities.
     * @param sosFilterCaps
     *            SOS scalar filter information
     */
    private void setScalarFilterCapabilities(
                    ScalarCapabilitiesType scalarCapabilitiesType,
                    org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

            if (sosFilterCaps.getComparisonOperators() != null
                            && !sosFilterCaps.getComparisonOperators().isEmpty()) {
                    ComparisonOperatorsType scalarOps = scalarCapabilitiesType
                                    .addNewComparisonOperators();
                    for (ComparisonOperator operator : sosFilterCaps
                                    .getComparisonOperators()) {
                            scalarOps.addNewComparisonOperator().setName(
                                            getEnum4ComparisonOperator(operator));
                    }
            }
    }

    /**
     * Set the IdFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param idCapabilitiesType
     *            FES IdCapabilities.
     */
    private void setIdFilterCapabilities(IdCapabilitiesType idCapabilitiesType) {
            idCapabilitiesType.addNewResourceIdentifier();
    }

    /**
     * Get the FES spatial operator name for SOS spatial operator
     * 
     * @param spatialOperator
     *            SOS spatial operator
     * @return FES spatial operator name
     */
    private String getEnum4SpatialOperator(SpatialOperator spatialOperator) {
            switch (spatialOperator) {
            case BBOX:
                    return SpatialOperatorNameTypeImpl.BBOX.toString();
            case Beyond:
                    return SpatialOperatorNameTypeImpl.BEYOND.toString();
            case Contains:
                    return SpatialOperatorNameTypeImpl.CONTAINS.toString();
            case Crosses:
                    return SpatialOperatorNameTypeImpl.CROSSES.toString();
            case Disjoint:
                    return SpatialOperatorNameTypeImpl.DISJOINT.toString();
            case DWithin:
                    return SpatialOperatorNameTypeImpl.D_WITHIN.toString();
            case Equals:
                    return SpatialOperatorNameTypeImpl.EQUALS.toString();
            case Intersects:
                    return SpatialOperatorNameTypeImpl.INTERSECTS.toString();
            case Overlaps:
                    return SpatialOperatorNameTypeImpl.OVERLAPS.toString();
            case Touches:
                    return SpatialOperatorNameTypeImpl.TOUCHES.toString();
            case Within:
                    return SpatialOperatorNameTypeImpl.WITHIN.toString();
            default:
                    break;
            }
            return null;
    }

    /**
     * Get the FES temporal operator name for SOS temporal operator
     * 
     * @param temporalOperator
     *            SOS temporal operator
     * @return FES temporal operator name
     */
    private String getEnum4TemporalOperator(TimeOperator temporalOperator) {
            switch (temporalOperator) {
            case TM_After:
                    return TemporalOperatorNameTypeImpl.AFTER.toString();
            case TM_Before:
                    return TemporalOperatorNameTypeImpl.BEFORE.toString();
            case TM_Begins:
                    return TemporalOperatorNameTypeImpl.BEGINS.toString();
            case TM_BegunBy:
                    return TemporalOperatorNameTypeImpl.BEGUN_BY.toString();
            case TM_Contains:
                    return TemporalOperatorNameTypeImpl.T_CONTAINS.toString();
            case TM_During:
                    return TemporalOperatorNameTypeImpl.DURING.toString();
            case TM_EndedBy:
                    return TemporalOperatorNameTypeImpl.ENDED_BY.toString();
            case TM_Ends:
                    return TemporalOperatorNameTypeImpl.ENDS.toString();
            case TM_Equals:
                    return TemporalOperatorNameTypeImpl.T_EQUALS.toString();
            case TM_Meets:
                    return TemporalOperatorNameTypeImpl.MEETS.toString();
            case TM_MetBy:
                    return TemporalOperatorNameTypeImpl.MET_BY.toString();
            case TM_OverlappedBy:
                    return TemporalOperatorNameTypeImpl.OVERLAPPED_BY.toString();
            case TM_Overlaps:
                    return TemporalOperatorNameTypeImpl.T_OVERLAPS.toString();
            default:
                    break;
            }
            return null;
    }

    /**
     * Get the FES comparison operator name for SOS comparison operator
     * 
     * @param comparisonOperator
     *            SOS comparison operator
     * @return FES comparison operator name
     */
    private String getEnum4ComparisonOperator(
                    ComparisonOperator comparisonOperator) {
            switch (comparisonOperator) {
            case PropertyIsBetween:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_BETWEEN
                                    .toString();
            case PropertyIsEqualTo:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_EQUAL_TO
                                    .toString();
            case PropertyIsGreaterThan:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_GREATER_THAN
                                    .toString();
            case PropertyIsGreaterThanOrEqualTo:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO
                                    .toString();
            case PropertyIsLessThan:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LESS_THAN
                                    .toString();
            case PropertyIsLessThanOrEqualTo:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO
                                    .toString();
            case PropertyIsLike:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LIKE.toString();
            case PropertyIsNil:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NIL.toString();
            case PropertyIsNotEqualTo:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NOT_EQUAL_TO
                                    .toString();
            case PropertyIsNull:
                    return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NULL.toString();
            default:
                    break;
            }
            return null;
    }

}
