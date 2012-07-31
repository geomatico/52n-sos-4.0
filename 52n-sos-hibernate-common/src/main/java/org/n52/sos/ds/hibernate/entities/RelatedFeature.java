package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * RelatedFeature generated by hbm2java
 */
public class RelatedFeature implements java.io.Serializable {

	private long relatedFeatureId;
	private String identifier;
	private Set relatedFeatureRoles = new HashSet(0);
	private Set offerings = new HashSet(0);

	public RelatedFeature() {
	}

	public RelatedFeature(long relatedFeatureId) {
		this.relatedFeatureId = relatedFeatureId;
	}

	public RelatedFeature(long relatedFeatureId, String identifier,
			Set relatedFeatureRoles, Set offerings) {
		this.relatedFeatureId = relatedFeatureId;
		this.identifier = identifier;
		this.relatedFeatureRoles = relatedFeatureRoles;
		this.offerings = offerings;
	}

	public long getRelatedFeatureId() {
		return this.relatedFeatureId;
	}

	public void setRelatedFeatureId(long relatedFeatureId) {
		this.relatedFeatureId = relatedFeatureId;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Set getRelatedFeatureRoles() {
		return this.relatedFeatureRoles;
	}

	public void setRelatedFeatureRoles(Set relatedFeatureRoles) {
		this.relatedFeatureRoles = relatedFeatureRoles;
	}

	public Set getOfferings() {
		return this.offerings;
	}

	public void setOfferings(Set offerings) {
		this.offerings = offerings;
	}

}
