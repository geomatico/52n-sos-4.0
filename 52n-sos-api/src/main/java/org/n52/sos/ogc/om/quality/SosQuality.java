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

package org.n52.sos.ogc.om.quality;

/**
 * class which represents a simple quantitative data quality element
 * 
 */
public class SosQuality {

    /** name of the result value */
    private String resultName;

    /** unit of the result value */
    private String resultUnit;

    /** value of the quality result */
    private String resultValue;

    /** type of the quality object */
    private QualityType qualityType;

    /**
     * constructor
     * 
     * @param resultValueType
     * @param resultValueUnit
     * @param resultValue
     * @param qualityType
     */
    public SosQuality(String resultName, String resultUnit, String resultValue, QualityType qualityType) {
        this.resultName = resultName;
        this.resultUnit = resultUnit;
        this.resultValue = resultValue;
        this.qualityType = qualityType;
    }

    /**
     * @return the resultValue
     */
    public String getResultValue() {
        return resultValue;
    }

    /**
     * @param resultValue
     *            the resultValue to set
     */
    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    /**
     * @return the resultName
     */
    public String getResultName() {
        return resultName;
    }

    /**
     * @param resultName
     *            the resultName to set
     */
    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    /**
     * @return the resultValueUnit
     */
    public String getResultUnit() {
        return resultUnit;
    }

    /**
     * @param resultValueUnit
     *            the resultValueUnit to set
     */
    public void setResultUnit(String resultValueUnit) {
        this.resultUnit = resultValueUnit;
    }

    /**
     * quality type
     */
    public enum QualityType {
        quantity, category, text
    }

    /**
     * @return the qualityType
     */
    public QualityType getQualityType() {
        return qualityType;
    }

    /**
     * @param qualityType
     *            the qualityType to set
     */
    public void setQualityType(QualityType qualityType) {
        this.qualityType = qualityType;
    }

}
