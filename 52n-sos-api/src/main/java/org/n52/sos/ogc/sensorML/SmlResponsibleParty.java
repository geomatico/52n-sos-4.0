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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for sml:ResponsibleParty
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SmlResponsibleParty implements SmlContact{

	private String individualName;
	
	private String organizationName;
	
	private String positionName;
	
	private List<String> phoneVoice;
	
	private List<String> phoneFax;
	
	private List<String> deliveryPoints;
	
	private String city;
	
	private String administrativeArea;
	
	private String postalCode;
	
	private String country;
	
	private String email;
	
	private List<String> onlineResources;
	
	private String hoursOfService;
	
	private String contactInstructions;
	
	public SmlResponsibleParty() {}
	
	public SmlResponsibleParty(final String invidualName, final String organizationName, final String positionName, final List<String> phoneVoice, final List<String> phoneFax, final List<String> deliveryPoint, final String city,
			final String administrativeArea, final String postalCode, final String country, final String email, final List<String> onlineResource, final String hoursOfService, final String contactInstructions) {
		individualName = invidualName;
		this.organizationName = organizationName;
		this.positionName = positionName;
		this.phoneVoice = phoneVoice;
		this.phoneFax = phoneFax;
		deliveryPoints = deliveryPoint;
		this.city = city;
		this.administrativeArea = administrativeArea;
		this.postalCode = postalCode;
		this.country = country;
		this.email = email;
		onlineResources = onlineResource;
		this.hoursOfService = hoursOfService;
		this.contactInstructions = contactInstructions;
	}
	
	public boolean isSetIndividualName() {
		return individualName != null && !individualName.isEmpty();
	}

	public String getInvidualName()
	{
		return individualName;
	}

	public SmlResponsibleParty setIndividualName(final String invidualName)
	{
		individualName = invidualName;
		return this;
	}
	
	public boolean isSetOrganizationName() {
		return organizationName != null && !organizationName.isEmpty();
	}

	public String getOrganizationName()
	{
		return organizationName;
	}

	public SmlResponsibleParty setOrganizationName(final String organizationName)
	{
		this.organizationName = organizationName;
		return this;
	}
	
	public boolean isSetPositionName() {
		return positionName != null && !positionName.isEmpty();
	}

	public String getPositionName()
	{
		return positionName;
	}

	public SmlResponsibleParty setPositionName(final String positionName)
	{
		this.positionName = positionName;
		return this;
	}
	
	public boolean isSetPhoneVoice() {
		return phoneVoice != null && !phoneVoice.isEmpty();
	}

	public List<String> getPhoneVoice()
	{
		return phoneVoice;
	}

	public SmlResponsibleParty setPhoneVoice(final List<String> phoneVoice)
	{
		if (isSetPhoneVoice()) 
		{
			this.phoneVoice.addAll(phoneVoice);
		}
		else 
		{
			this.phoneVoice = phoneVoice;
		}
		return this;
	}
	
	public SmlResponsibleParty addPhoneVoice(final String phoneVoice)
	{
		if (!isSetPhoneVoice())
		{ 
			this.phoneVoice = new ArrayList<String>();
		}
		this.phoneVoice.add(phoneVoice);
		return this;
	}
	
	public boolean isSetPhoneFax() {
		return phoneFax != null && !phoneFax.isEmpty();
	}

	public List<String> getPhoneFax()
	{
		return phoneFax;
	}
	
	public SmlResponsibleParty addPhoneFax(final String phoneFax)
	{
		if(!isSetPhoneFax())
		{
			this.phoneFax = new ArrayList<String>();
		}
		this.phoneFax.add(phoneFax);
		return this;
	}

	public SmlResponsibleParty setPhoneFax(final List<String> phoneFax)
	{
		if (isSetPhoneFax())
		{
			this.phoneFax.addAll(phoneFax);
		}
		else
		{
			this.phoneFax = phoneFax;
		}
		return this;
	}
	
	public boolean isSetDeliveryPoint() {
		return deliveryPoints != null && !deliveryPoints.isEmpty();
	}

	public List<String> getDeliveryPoint()
	{
		return deliveryPoints;
	}

	public SmlResponsibleParty setDeliveryPoint(final List<String> deliveryPoints)
	{
		if (isSetDeliveryPoint())
		{
			this.deliveryPoints.addAll(deliveryPoints);
		}
		else
		{
			this.deliveryPoints = deliveryPoints;
		}
		return this;
	}
	
	public SmlResponsibleParty addDeliveryPoint(final String deliveryPoint)
	{
		if (!isSetDeliveryPoint())
		{
			deliveryPoints = new ArrayList<String>();
		}
		deliveryPoints.add(deliveryPoint);
		return this;
	}

	public boolean isSetCity() {
		return city != null && !city.isEmpty();
	}
	
	public String getCity()
	{
		return city;
	}

	public SmlResponsibleParty setCity(final String city)
	{
		this.city = city;
		return this;
	}
	
	public boolean isSetAdministrativeArea() {
		return administrativeArea != null && !administrativeArea.isEmpty();
	}

	public String getAdministrativeArea()
	{
		return administrativeArea;
	}

	public SmlResponsibleParty setAdministrativeArea(final String administrativeArea)
	{
		this.administrativeArea = administrativeArea;
		return this;
	}

	public boolean isSetPostalCode() {
		return postalCode != null && !postalCode.isEmpty();
	}
	
	public String getPostalCode()
	{
		return postalCode;
	}

	public SmlResponsibleParty setPostalCode(final String postalCode)
	{
		this.postalCode = postalCode;
		return this;
	}
	
	public boolean isSetCountry() {
		return country != null && !country.isEmpty();
	}

	public String getCountry()
	{
		return country;
	}

	public SmlResponsibleParty setCountry(final String country)
	{
		this.country = country;
		return this;
	}
	
	public boolean isSetEmail() {
		return email != null && !email.isEmpty();
	}

	public String getEmail()
	{
		return email;
	}

	public SmlResponsibleParty setEmail(final String email)
	{
		this.email = email;
		return this;
	}
	
	public boolean isSetOnlineResources() {
		return onlineResources != null && !onlineResources.isEmpty();
	}

	public List<String> getOnlineResources()
	{
		return onlineResources;
	}

	public SmlResponsibleParty setOnlineResource(final List<String> onlineResources)
	{
		if (isSetOnlineResources())
		{
			this.onlineResources.addAll(onlineResources);
		}
		else
		{
			this.onlineResources = onlineResources;
		}
		return this;
	}
	
	public SmlResponsibleParty addOnlineResource(final String onlineResource)
	{
		if(!isSetOnlineResources())
		{
			onlineResources = new ArrayList<String>();
		}
		onlineResources.add(onlineResource);
		return this;
	}
	
	public boolean isSetHoursOfService() {
		return hoursOfService != null && !hoursOfService.isEmpty();
	}

	public String getHoursOfService()
	{
		return hoursOfService;
	}

	public SmlResponsibleParty setHoursOfService(final String hoursOfService)
	{
		this.hoursOfService = hoursOfService;
		return this;
	}
	
	public boolean isSetContactInstructions() {
		return contactInstructions != null && !contactInstructions.isEmpty();
	}

	public String getContactInstructions()
	{
		return contactInstructions;
	}

	public SmlResponsibleParty setContactInstructions(final String contactInstructions)
	{
		this.contactInstructions = contactInstructions;
		return this;
	}
	
	public boolean isSetContactInfo() {
		return isSetPhone() || isSetAddress() || isSetOnlineResources() || isSetHoursOfService() || isSetContactInstructions();
	}
	
	public boolean isSetAddress() {
		return isSetDeliveryPoint() || isSetCity() || isSetAdministrativeArea() || isSetPostalCode() || isSetCountry() || isSetEmail();
	}
	
	public boolean isSetPhone() {
		return isSetPhoneFax() || isSetPhoneVoice();
	}

}
