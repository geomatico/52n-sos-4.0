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
package org.n52.sos.ogc.sensorML.elements;

import org.n52.sos.ogc.gml.time.TimeInstant;

public class SmlDocumentation extends AbstractSmlDocumentation {
    
    private String version;
    
    private TimeInstant date;
    
    private String contact;
    
    private String format;
    
    private String onlineResource;

    public String getVersion() {
        return version;
    }

    public TimeInstant getDate() {
        return date;
    }

    public String getContact() {
        return contact;
    }

    public String getFormat() {
        return format;
    }

    public String getOnlineResource() {
        return onlineResource;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDate(TimeInstant date) {
        this.date = date;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setOnlineResource(String onlineResource) {
        this.onlineResource = onlineResource;
    }
    
    public boolean isSetVersion() {
        return version != null && !version.isEmpty();
    }
    
    public boolean isSetDate() {
        return date != null;
    }
    
    public boolean isSetContact() {
        return contact != null && !contact.isEmpty();
    }
    
    public boolean isSetFormat() {
        return format != null && !format.isEmpty();
    }
    
    public boolean isSeOnlineResource() {
        return onlineResource != null && !onlineResource.isEmpty();
    }
    

}
