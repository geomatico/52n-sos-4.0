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
package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

public class SosServiceIdentification {

    private XmlObject serviceIdentification;
	
	private String title;
	private String abstrakt;
	private String serviceType;
	private String fees;
	private String accessConstraints;

    private Collection<String> versions;
    private List<String> profiles;
    private List<String> keywords;

    public XmlObject getServiceIdentification() {
        return serviceIdentification;
    }

    public void setServiceIdentification(XmlObject serviceIdentification) {
        this.serviceIdentification = serviceIdentification;
    }

    public Collection<String> getVersions() {
        return versions;
    }

    public void setVersions(Collection<String> versions) {
        this.versions = versions;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstract() {
		return abstrakt;
	}

	public void setAbstract(String abstrakt) {
		this.abstrakt = abstrakt;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getFees() {
		return fees;
	}

	public void setFees(String fees) {
		this.fees = fees;
	}

	public String getAccessConstraints() {
		return accessConstraints;
	}

	public void setAccessConstraints(String accessConstraints) {
		this.accessConstraints = accessConstraints;
	}

}
