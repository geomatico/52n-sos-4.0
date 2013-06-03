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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SweDataArrayBuilder {

	private SweDataRecord elementType;
	private String[] encodingParameter;
	private List<List<String>> blocks;
	
	public static SweDataArrayBuilder aSweDataArray()
	{
		return new SweDataArrayBuilder();
	}

	public SweDataArrayBuilder setElementType(SweDataRecord elementType)
	{
		this.elementType = elementType;
		return this;
	}

	/**
	 * 
	 * @param encodingParameter type, block separator, token separator, decimal separator
	 * @return
	 */
	public SweDataArrayBuilder setEncoding(String... encodingParameter)
	{
		this.encodingParameter = encodingParameter;
		return this;
	}
	
	public SweDataArrayBuilder addBlock(String... tokens)
	{
		if (tokens != null && tokens.length > 0)
		{
			if (blocks == null)
			{
				blocks = new ArrayList<List<String>>();
			}
			blocks.add(Arrays.asList(tokens));
		}
		return this;
	}
	
	public SweDataArray build()
	{
		SweDataArray dataArray = new SweDataArray();
		dataArray.setElementType(elementType);
		if (encodingParameter != null && encodingParameter.length == 4)
		{
			if (encodingParameter[0].equals("text"))
			{
				SweTextEncoding encoding = new SweTextEncoding();
				encoding.setBlockSeparator(encodingParameter[1]);
				encoding.setTokenSeparator(encodingParameter[2]);
				encoding.setDecimalSeparator(encodingParameter[3]);
				dataArray.setEncoding(encoding);
			}
		}
		if (blocks != null && !blocks.isEmpty())
		{
			for (List<String> block : blocks) {
				dataArray.add(block);
			}
		}
		return dataArray;
	}

}
