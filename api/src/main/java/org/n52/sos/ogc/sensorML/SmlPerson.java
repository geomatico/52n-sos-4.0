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
 * Implementation for sml:Person
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class SmlPerson implements SmlContact {

	private String affiliation;
	
	private String email;
	
	private String name;
	
	private String phoneNumber;
	
	private String surname;
	
	private String userID;
	
	public SmlPerson() {}

	public SmlPerson(final String surname, final String name, final String userID, final String affiliation, final String phoneNumber, final String email) {
		this.surname = surname;
		this.name = name;
		this.userID = userID;
		this.affiliation = affiliation;
		this.phoneNumber = phoneNumber;
		this.email = email;
	}
	
	public String getAffiliation()
	{
		return affiliation;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPhoneNumber()
	{
		return phoneNumber;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public String getUserID()
	{
		return userID;
	}

	public boolean isSetAffiliation() {
		return affiliation != null && !affiliation.isEmpty();
	}

	public boolean isSetEmail() {
		return email != null && !email.isEmpty();
	}

	public boolean isSetName() {
		return name != null && !name.isEmpty();
	}

	public boolean isSetPhoneNumber() {
		return phoneNumber != null && !phoneNumber.isEmpty();
	}

	public boolean isSetSurname() {
		return surname != null && !surname.isEmpty();
	}

	public boolean isSetUserID() {
		return userID != null && !userID.isEmpty();
	}

	public SmlContact setAffiliation(final String affiliation)
	{
		this.affiliation = affiliation;
		return this;
	}

	public SmlContact setEmail(final String email)
	{
		this.email = email;
		return this;
	}

	public SmlContact setName(final String name)
	{
		this.name = name;
		return this;
	}

	public SmlContact setPhoneNumber(final String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
		return this;
	}

	public SmlContact setSurname(final String surname)
	{
		this.surname = surname;
		return this;
	}

	public SmlContact setUserID(final String userID)
	{
		this.userID = userID;
		return this;
	}
	
}
