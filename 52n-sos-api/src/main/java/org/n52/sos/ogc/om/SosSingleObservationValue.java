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
package org.n52.sos.ogc.om;

import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.om.values.IValue;

public class SosSingleObservationValue<T> implements IObservationValue<IValue<T>> {

    private static final long serialVersionUID = 52L;

    private ITime phenomenonTime;

    private IValue<T> value;

    private List<SosQuality> qualityList;

    public SosSingleObservationValue() {
    }

    public SosSingleObservationValue(IValue<T> value) {
        this.value = value;
    }

    public SosSingleObservationValue(ITime phenomenonTime, IValue<T> value, List<SosQuality> qualityList) {
        this.phenomenonTime = phenomenonTime;
        this.value = value;
        this.qualityList = qualityList;
    }

    public SosSingleObservationValue(ITime phenomenonTime, IValue<T> value) {
        this.phenomenonTime = phenomenonTime;
        this.value = value;
    }

    @Override
    public ITime getPhenomenonTime() {
        return phenomenonTime;
    }

    @Override
    public IValue<T> getValue() {
        return value;
    }

    @Override
    public void setValue(IValue<T> value) {
        this.value = value;
    }

    public void setQualityList(List<SosQuality> qualityList) {
        this.qualityList = qualityList;
    }

    public List<SosQuality> getQualityList() {
        return qualityList;
    }

    @Override
    public void setPhenomenonTime(ITime phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }
}
