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
package org.n52.sos.ogc.filter;

import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOS class for result filter
 * 
 */
public class ComparisonFilter {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonFilter.class);

    /**
     * property name
     */
    private String propertyName;

    /**
     * Filter operator
     */
    private ComparisonOperator operator;

    /**
     * filter value
     */
    private String value;

    /**
     * filter value for between filter
     */
    private String valueUpper;

    /**
     * escape character
     */
    private String escapeString;

    /**
     * wild card character
     */
    private String wildCard;

    /**
     * single char character
     */
    private String singleChar;

    /**
     * constructor
     * 
     * @param operatorp
     *            Filter operator
     * @param propertyNamep
     *            property name
     * @param valuep
     *            value
     */
    public ComparisonFilter(ComparisonOperator operatorp, String propertyNamep, String valuep) {
        this.operator = operatorp;
        this.value = valuep;
        this.propertyName = propertyNamep;
    }

    /**
     * constructor
     * 
     * @param operatorp
     *            Filter operator
     * @param propertyNamep
     *            property name
     * @param valuep
     *            value
     * @param valueUpperp
     *            upper value for between filter
     * @throws OwsExceptionReport
     *             If the constructor is not valid for operator
     */
    public ComparisonFilter(ComparisonOperator operatorp, String propertyNamep, String valuep, String valueUpperp)
            throws OwsExceptionReport {
        if (operatorp == ComparisonOperator.PropertyIsBetween) {
            this.operator = operatorp;
            this.value = valuep;
            this.valueUpper = valueUpperp;
            this.propertyName = propertyNamep;
        } else {
            String exceptionText =
                    "Use other constructor for ComparisonFilter! This constructor could only"
                            + "be used for operator 'PropertyIsBetween'";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport();
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    /**
     * constructor
     * 
     * @param operatorp
     *            Filter operator
     * @param propertyNamep
     *            property name
     * @param valuep
     *            value
     * @param valueUpperp
     *            upper value for between filter
     * @param escapeStringp
     *            Escape characters
     * @throws OwsExceptionReport
     *             If the constructor is not valid for operator
     */
    public ComparisonFilter(ComparisonOperator operatorp, String propertyNamep, String valuep, String valueUpperp,
            String escapeStringp) throws OwsExceptionReport {
        if (operatorp == ComparisonOperator.PropertyIsLike) {
            this.operator = operatorp;
            this.value = valuep;
            this.valueUpper = valueUpperp;
            this.propertyName = propertyNamep;
            this.escapeString = escapeStringp;
        } else {
            String exceptionText =
                    "Use other constructor for ComparisonFilter! This constructor could only"
                            + "be used for operator 'PropertyIsLike'";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    /**
     * default constructor
     */
    public ComparisonFilter() {
    }

    /**
     * Get filter value
     * 
     * @return filter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get upper filter value
     * 
     * @return upper filter value
     */
    public String getValueUpper() {
        return valueUpper;
    }

    /**
     * Set filter value
     * 
     * @param value
     *            filter value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set upper filter value
     * 
     * @param valueUpper
     *            upper filter value
     */
    public void setValueUpper(String valueUpper) {
        this.valueUpper = valueUpper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String result = "Spatial filter: ";
        if (valueUpper != null) {
            return result + propertyName + " " + value + " " + operator.name() + " " + valueUpper;
        } else {
            return result + propertyName + " " + operator.name() + " " + value;
        }
    }

    /**
     * Get filter operator
     * 
     * @return filter operator
     */
    public ComparisonOperator getOperator() {
        return operator;
    }

    /**
     * Set filter operator
     * 
     * @param operator
     *            filter operator
     */
    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    /**
     * Get filter property name
     * 
     * @return filter property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Set filter property name
     * 
     * @param propertyName
     *            filter property name
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Get escape characters
     * 
     * @return escape characters
     */
    public String getEscapeString() {
        return escapeString;
    }

    /**
     * Set escape characters
     * 
     * @param escapeString
     *            escape characters
     */
    public void setEscapeString(String escapeString) {
        this.escapeString = escapeString;
    }

    /**
     * Get wild card character
     * 
     * @return wild card character
     */
    public String getWildCard() {
        return wildCard;
    }

    /**
     * Set wild card character
     * 
     * @param wildCard
     *            wild card character
     */
    public void setWildCard(String wildCard) {
        this.wildCard = wildCard;
    }

    /**
     * Get single char character
     * 
     * @return single char character
     */
    public String getSingleChar() {
        return singleChar;
    }

    /**
     * Set single char character
     * 
     * @param singleChar
     *            single char character
     */
    public void setSingleChar(String singleChar) {
        this.singleChar = singleChar;
    }

}
