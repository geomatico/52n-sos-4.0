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
package org.n52.sos.ogc.om;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.values.IMultiValue;
import org.n52.sos.ogc.om.values.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosMultiObservationValues<T> implements IObservationValue<IValue<T>> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SosMultiObservationValues.class);

    private static final long serialVersionUID = 1L;

    private IValue<T> values;

    private ITime phenomenonTime;

    @Override
    public ITime getPhenomenonTime() {
        if (phenomenonTime == null && values != null && values instanceof IMultiValue) {
            phenomenonTime = ((IMultiValue)values).getPhenomenonTime();
        }
        return phenomenonTime;
    }

    @Override
    public IValue<T> getValue() {

        return values;
    }

    @Override
    public void setValue(IValue<T> value) {
        this.values = value;
    }

    @Override
    public void setPhenomenonTime(ITime phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }


}
