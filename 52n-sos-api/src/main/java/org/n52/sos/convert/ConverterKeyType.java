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
package org.n52.sos.convert;

public class ConverterKeyType implements Comparable<ConverterKeyType> {

    private String fromNamespace;

    private String toNamespace;

    public ConverterKeyType(String fromNamespace, String toNamespace) {
        super();
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
    }

    public String getFromNamespace() {
        return fromNamespace;
    }

    public String getToNamespace() {
        return toNamespace;
    }

    @Override
    public int compareTo(ConverterKeyType o) {
        if (o instanceof ConverterKeyType) {
            ConverterKeyType toCheck = (ConverterKeyType) o;
            if (checkParameter(fromNamespace, toCheck.fromNamespace)
                    && checkParameter(toNamespace, toCheck.toNamespace)) {
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
        if (paramObject instanceof ConverterKeyType) {
            ConverterKeyType toCheck = (ConverterKeyType) paramObject;
            return (checkParameter(fromNamespace, toCheck.fromNamespace) && checkParameter(toNamespace,
                    toCheck.toNamespace));
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
        if (fromNamespace != null) {
            hash = 31 * hash + fromNamespace.hashCode();
        }
        if (toNamespace != null) {
            hash += 31 * hash + toNamespace.hashCode();
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
        string.append("DecoderKey(");
        string.append(fromNamespace + ",");
        string.append(toNamespace);
        string.append(")");
        return string.toString();
    }

}
