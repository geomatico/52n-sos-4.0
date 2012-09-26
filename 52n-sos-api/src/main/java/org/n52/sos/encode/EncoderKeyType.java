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
package org.n52.sos.encode;

public class EncoderKeyType implements Comparable<EncoderKeyType> {

    private String namespace;

    private String service;

    private String version;

    public EncoderKeyType(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public int compareTo(EncoderKeyType o) {
        if (o instanceof EncoderKeyType) {
            EncoderKeyType toCheck = (EncoderKeyType) o;
            if (checkParameter(service, toCheck.service) && checkParameter(version, toCheck.version)
                    && checkParameter(namespace, toCheck.namespace)) {
                return 0;
            }
            return 1;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof EncoderKeyType) {
            EncoderKeyType toCheck = (EncoderKeyType) paramObject;
            return (checkParameter(service, toCheck.service) && checkParameter(version, toCheck.version) && checkParameter(
                    namespace, toCheck.namespace));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        if (service != null) {
            hash = 31 * hash + service.hashCode();
        }
        if (version != null) {
            hash += 31 * hash + version.hashCode();
        }
        if (namespace != null) {
            hash += 31 * hash + namespace.hashCode();
        }
        return hash;
    }

    private boolean checkParameter(String localParameter, String parameterToCheck) {
        if ((localParameter == null && parameterToCheck == null)
                || (localParameter != null && parameterToCheck != null && localParameter.equals(parameterToCheck))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("EncoderKey(");
        string.append(service + ",");
        string.append(version + ",");
        string.append(namespace);
        string.append(")");
        return string.toString();
    }

}
