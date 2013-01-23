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
package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

/**
 * ObservationTemplate generated by hbm2java
 */
public class ObservationTemplate implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long observationTemplateId;

//    private Request request;

//    private Procedure procedure;

    private String observationTemplate;

    public ObservationTemplate() {
    }

//    public ObservationTemplate(long observationTemplateId, Request request, Procedure procedure) {
//        this.observationTemplateId = observationTemplateId;
//        this.request = request;
//        this.procedure = procedure;
//    }
//
//    public ObservationTemplate(long observationTemplateId, Request request, Procedure procedure,
//            String observationTemplate) {
//        this.observationTemplateId = observationTemplateId;
//        this.request = request;
//        this.procedure = procedure;
//        this.observationTemplate = observationTemplate;
//    }

    public long getObservationTemplateId() {
        return this.observationTemplateId;
    }

    public void setObservationTemplateId(long observationTemplateId) {
        this.observationTemplateId = observationTemplateId;
    }

//    public Request getRequest() {
//        return this.request;
//    }
//
//    public void setRequest(Request request) {
//        this.request = request;
//    }
//
//    public Procedure getProcedure() {
//        return this.procedure;
//    }
//
//    public void setProcedure(Procedure procedure) {
//        this.procedure = procedure;
//    }

    public String getObservationTemplate() {
        return this.observationTemplate;
    }

    public void setObservationTemplate(String observationTemplate) {
        this.observationTemplate = observationTemplate;
    }

}
