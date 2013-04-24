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
package org.n52.sos.ogc.sensorML;

/**
 * Implementation for sml:RulesDefinition
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class RulesDefinition {
	
	private String description;
	
	public RulesDefinition() {};
	
	public RulesDefinition(final String description)
	{
		this.description = description;
	}
	
	/**
	 * @return <tt>true</tt>, if description is set and not empty.
	 */
	public boolean isSetDescription()
	{
		return description != null && !description.isEmpty();
	}
	
	/**
	 * 
	 * @param description a {@link String} holding the "[t]extual description of the i/o structure" (Source: SensorML 1.0.1).
	 * @return the {@link RulesDefinition} object instance
	 */
	public RulesDefinition setDescription(final String description)
	{
		this.description = description;
		return this;
	}
	
	/**
	 * 
	 * @return Textual description of the i/o structure (Source: SensorML 1.0.1)
	 */
	public String getDescription()
	{
		return description;
	}

}
