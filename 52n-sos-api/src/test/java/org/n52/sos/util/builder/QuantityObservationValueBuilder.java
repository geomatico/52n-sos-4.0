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
package org.n52.sos.util.builder;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class QuantityObservationValueBuilder {

	public static QuantityObservationValueBuilder aQuantityValue()
	{
		return new QuantityObservationValueBuilder();
	}

	private IValue<BigDecimal> quantity;
	private long phenomenonTime;

	public QuantityObservationValueBuilder setValue(QuantityValue quantity)
	{
		this.quantity = quantity;
		return this;
	}

	public SosSingleObservationValue<BigDecimal> build()
	{
		SosSingleObservationValue<BigDecimal> value = new SosSingleObservationValue<BigDecimal>();
		value.setValue(quantity);
		value.setPhenomenonTime(new TimeInstant(new DateTime(phenomenonTime)));
		return value;
	}

	public QuantityObservationValueBuilder setPhenomenonTime(long currentTimeMillis)
	{
		this.phenomenonTime = currentTimeMillis;
		return this;
	}

}
