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
package org.n52.sos.ogc.gml;

public class CodeType {
    
    private String value;
    
    private String codeSpace = "";

    public CodeType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setValue(final String value) {
        this.value = value;
    }

	public void setCodeSpace(final String codeSpace) {
        this.codeSpace = codeSpace;
    }
    
    public boolean isSetValue() {
        return value != null && !value.isEmpty();
    }

    public boolean isSetCodeSpace() {
        return codeSpace != null && !codeSpace.isEmpty();
    }

    @Override
    public String toString()
    {
    	return String.format("CodeType [value=%s, codeSpace=%s]", value, codeSpace);
    }
}
