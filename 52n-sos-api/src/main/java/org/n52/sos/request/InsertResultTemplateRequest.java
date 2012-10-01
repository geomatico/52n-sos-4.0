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
package org.n52.sos.request;

import org.joda.time.DateTime;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.util.SosHelper;


public class InsertResultTemplateRequest extends AbstractServiceRequest {

    private final String operationName = Sos2Constants.Operations.InsertResultTemplate.name();
    
    private String identifier;
    
    private SosObservationConstellation observationConstellation;
    
    private SosResultStructure resultStructure;
    
    private SosResultEncoding resultEncoding;
    
    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setIdentifier(String identifier) {
       this.identifier = identifier;
    }

    public void setObservationTemplate(SosObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    public void setResultStructure(SosResultStructure resultStructure) {
        this.resultStructure = resultStructure;
    }

    public void setResultEncoding(SosResultEncoding resultEncoding) {
        this.resultEncoding = resultEncoding;
    }

    public String getIdentifier() {
        if (identifier == null || (identifier != null && identifier.isEmpty())) {
            StringBuilder builder = new StringBuilder();
            builder.append(resultStructure.getXml());
            builder.append(new DateTime().getMillis());
            identifier = SosHelper.generateID(builder.toString());
        }
        return identifier;
    }

    public SosObservationConstellation getObservationConstellation() {
        return observationConstellation;
    }

    public SosResultStructure getResultStructure() {
        return resultStructure;
    }

    public SosResultEncoding getResultEncoding() {
        return resultEncoding;
    }

    public void setObservationConstellation(SosObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }
    
    

}
