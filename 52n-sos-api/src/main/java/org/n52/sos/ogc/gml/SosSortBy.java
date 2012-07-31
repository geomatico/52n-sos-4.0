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

package org.n52.sos.ogc.gml;

import org.n52.sos.ogc.gml.GMLConstants.SortingOrder;

/**
 * class represents the gml:sortByType
 * 
 */
public class SosSortBy {

    /** name of the property, by which should be sorted */
    private String property;

    /**
     * order of the sorting (currently only ascending (ASC) or descending (DESC)
     */
    private SortingOrder order;

    /**
     * constructor
     * 
     * @param propertyp
     *            name of property, by which should be sorted
     * @param orderp
     *            sorting order (currently only ascending ('ASC') or descending
     *            ('DESC')
     */
    public SosSortBy(String propertyp, SortingOrder orderp) {
        this.property = propertyp;
        this.order = orderp;
    }

    /**
     *  default constructor
     */
    public SosSortBy() {
    }

    /**
     * 
     * @return Returns String representation with values of this object
     */
    public String toString() {
        return "Sort by " + property + " " + order;
    }

    /**
     * @return the order
     */
    public SortingOrder getOrder() {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    public void setOrder(SortingOrder order) {
        this.order = order;
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * @param property
     *            the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }
}
