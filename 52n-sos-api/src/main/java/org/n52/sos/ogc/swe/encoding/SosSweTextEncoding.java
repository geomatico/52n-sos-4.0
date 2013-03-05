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
package org.n52.sos.ogc.swe.encoding;

public class SosSweTextEncoding extends SosSweAbstractEncoding {
    
    private String blockSeparator;
    
    private String tokenSeparator;
    
    private String decimalSeparator;
    
    private Boolean collapseWhiteSpaces;
    
    public String getBlockSeparator() {
        return blockSeparator;
    }

    public String getTokenSeparator() {
        return tokenSeparator;
    }

    public void setBlockSeparator(String blockSeparator) {
        this.blockSeparator = blockSeparator;
    }

    public void setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
       this.decimalSeparator = decimalSeparator;
    }

    public void setCollapseWhiteSpaces(boolean collapseWhiteSpaces) {
        this.collapseWhiteSpaces = collapseWhiteSpaces?Boolean.TRUE:Boolean.FALSE;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public boolean isCollapseWhiteSpaces() {
        return collapseWhiteSpaces.booleanValue();
    }
    
    public boolean isSetCollapseWhiteSpaces() {
    	return collapseWhiteSpaces != null;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int hash = 5;
        hash = prime * hash + (this.getBlockSeparator() != null ? this.getBlockSeparator().hashCode() : 0);
        hash = prime * hash + (this.getTokenSeparator() != null ? this.getTokenSeparator().hashCode() : 0);
        hash = prime * hash + (this.getDecimalSeparator() != null ? this.getDecimalSeparator().hashCode() : 0);
        hash = prime * hash + (this.collapseWhiteSpaces != null ? this.collapseWhiteSpaces.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweTextEncoding other = (SosSweTextEncoding) obj;
        if ((this.getBlockSeparator() == null) ? (other.getBlockSeparator() != null) : !this.getBlockSeparator().equals(other.getBlockSeparator())) {
            return false;
        }
        if ((this.getTokenSeparator() == null) ? (other.getTokenSeparator() != null) : !this.getTokenSeparator().equals(other.getTokenSeparator())) {
            return false;
        }
        if ((this.getDecimalSeparator() == null) ? (other.getDecimalSeparator() != null) : !this.getDecimalSeparator().equals(other.getDecimalSeparator())) {
            return false;
        }
        if (this.collapseWhiteSpaces != other.collapseWhiteSpaces && (this.collapseWhiteSpaces == null || !this.collapseWhiteSpaces.equals(other.collapseWhiteSpaces))) {
            return false;
        }
        return true;
    }

}
