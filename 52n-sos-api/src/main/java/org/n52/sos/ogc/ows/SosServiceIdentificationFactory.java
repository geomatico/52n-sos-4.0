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
package org.n52.sos.ogc.ows;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.LazyThreadSafeFactory;
import org.n52.sos.util.XmlHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SosServiceIdentificationFactory extends LazyThreadSafeFactory<SosServiceIdentification> {
	private File file;
	private String[] keywords;
	private String title;
	private String description;
	private String serviceType;
	private String fees;
	private String constraints;

	public void setFile(File file) {
		this.file = file;
		setRecreate();
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
		setRecreate();
	}

	public void setKeywords(String keywords) {
		if (keywords != null) {
			String[] keywordArray = keywords.split(",");
			ArrayList<String> keywordList = new ArrayList<String>(keywordArray.length);
			for (String s : keywordArray) {
				if (s != null && !s.trim().isEmpty()) {
					keywordList.add(s.trim());
				}
			}
			setKeywords(keywordList.toArray(new String[keywordList.size()]));
		} else {
			setKeywords(new String[0]);
		}
	}

	public void setTitle(String title) {
		this.title = title;
		setRecreate();
	}

	public void setAbstract(String description) {
		this.description = description;
		setRecreate();
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
		setRecreate();
	}

	public void setFees(String fees) {
		this.fees = fees;
		setRecreate();
	}

	public void setConstraints(String constraints) {
		this.constraints = constraints;
		setRecreate();
	}

	@Override
	protected SosServiceIdentification create() throws OwsExceptionReport {
		SosServiceIdentification serviceIdentification = new SosServiceIdentification();
		if (this.file != null) {
			serviceIdentification.setServiceIdentification(XmlHelper.loadXmlDocumentFromFile(this.file));
		} else {
			serviceIdentification.setAbstract(this.description);
			serviceIdentification.setAccessConstraints(this.constraints);
			serviceIdentification.setFees(this.fees);
			serviceIdentification.setServiceType(this.serviceType);
			serviceIdentification.setTitle(this.title);
			serviceIdentification.setVersions(Configurator.getInstance().getServiceOperatorRepository().getSupportedVersions());
			serviceIdentification.setKeywords(Arrays.asList(this.keywords));
		}
		return serviceIdentification;
	}
}
