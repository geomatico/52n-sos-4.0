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
package org.n52.sos.ds;

import java.util.Collection;
import java.util.Set;

import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.response.GetResultTemplateResponse;


public abstract class AbstractGetResultTemplateDAO extends AbstractOperationDAO {
    public AbstractGetResultTemplateDAO() {
        super(Sos2Constants.Operations.GetResultTemplate.name());
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version)
            throws OwsExceptionReport {
        Set<String> resultTemplates = getCache().getResultTemplates();
        Collection<String> offerings = null;
        Collection<String> observableProperties = null;
        if (resultTemplates != null && !resultTemplates.isEmpty()) {
            offerings = getCache().getOfferingsWithResultTemplate();
            observableProperties = getCache().getObservablePropertiesWithResultTemplate();
        }
        opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultTemplateParams.offering, offerings);
        opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultTemplateParams.observedProperty, observableProperties);
    }

    public abstract GetResultTemplateResponse getResultTemplate(GetResultTemplateRequest request) throws OwsExceptionReport;

}
