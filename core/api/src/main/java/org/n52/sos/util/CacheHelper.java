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
package org.n52.sos.util;

import org.n52.sos.service.Configurator;

public final class CacheHelper {

    protected static Configurator getConfigurator() {
        return Configurator.getInstance();
    }

    public static String addPrefixOrGetOfferingIdentifier(String offering) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return getConfigurator().getDefaultOfferingPrefix() +  offering;
        } 
        return offering;
    }
    
    public static String removePrefixAndGetOfferingIdentifier(String offering) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return offering.replace(getConfigurator().getDefaultOfferingPrefix(), "");
        } 
        return offering;
    }

    public static String addPrefixOrGetProcedureIdentifier(String procedure) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return getConfigurator().getDefaultProcedurePrefix() +  procedure;
        } 
        return procedure;
    }
    
    public static String removePrefixAndGetProcedureIdentifier(String procedure) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return procedure.replace(getConfigurator().getDefaultProcedurePrefix(), "");
        } 
        return procedure;
    }

    public static String addPrefixOrGetFeatureIdentifier(String feature) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return getConfigurator().getDefaultFeaturePrefix() +  feature;
        } 
        return feature;
    }
    
    public static String removePrefixAndGetFeatureIdentifier(String feature) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return feature.replace(getConfigurator().getDefaultFeaturePrefix(), "");
        } 
        return feature;
    }

    public static String addPrefixOrGetObservablePropertyIdentifier(String observableProperty) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return getConfigurator().getDefaultObservablePropertyPrefix() +  observableProperty;
        } 
        return observableProperty;
    }
    
    public static String removePrefixAndGetObservablePropertyIdentifier(String observableProperty) {
        if (getConfigurator().isUseDefaultPrefixes()) {
            return observableProperty.replace(getConfigurator().getDefaultObservablePropertyPrefix(), "");
        } 
        return observableProperty;
    }

    private CacheHelper() {
    }
}
