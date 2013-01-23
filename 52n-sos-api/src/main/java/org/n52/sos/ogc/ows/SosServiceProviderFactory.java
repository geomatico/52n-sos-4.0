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

import java.io.File;
import org.n52.sos.util.LazyThreadSafeFactory;
import org.n52.sos.util.XmlHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SosServiceProviderFactory extends LazyThreadSafeFactory<SosServiceProvider> {
	private File file;
	private String name;
	private String site;
	private String individualName;
	private String positionName;
	private String phone;
	private String deliveryPoint;
	private String city;
	private String postalCode;
	private String country;
	private String mailAddress;
	private String administrativeArea;

	public void setFile(File file) {
		this.file = file;
		setRecreate();
	}

	public void setName(String name) {
		this.name = name;
		setRecreate();
	}

	public void setSite(String site) {
		this.site = site;
		setRecreate();
	}

	public void setIndividualName(String individualName) {
		this.individualName = individualName;
		setRecreate();
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
		setRecreate();
	}

	public void setPhone(String phone) {
		this.phone = phone;
		setRecreate();
	}

	public void setDeliveryPoint(String deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
		setRecreate();
	}

	public void setCity(String city) {
		this.city = city;
		setRecreate();
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
		setRecreate();
	}

	public void setCountry(String country) {
		this.country = country;
		setRecreate();
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
		setRecreate();
	}

	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
		setRecreate();
	}


	@Override
	protected SosServiceProvider create() throws OwsExceptionReport {
		SosServiceProvider serviceProvider = new SosServiceProvider();
		if (this.file != null) {
			serviceProvider.setServiceProvider(XmlHelper.loadXmlDocumentFromFile(this.file));
		} else {
			serviceProvider.setAdministrativeArea(this.administrativeArea);
			serviceProvider.setCity(this.city);
			serviceProvider.setCountry(this.country);
			serviceProvider.setDeliveryPoint(this.deliveryPoint);
			serviceProvider.setIndividualName(this.individualName);
			serviceProvider.setMailAddress(this.mailAddress);
			serviceProvider.setName(this.name);
			serviceProvider.setPhone(this.phone);
			serviceProvider.setPositionName(this.positionName);
			serviceProvider.setPostalCode(this.postalCode);
			serviceProvider.setSite(this.site);
		}
		return serviceProvider;
	}
}
