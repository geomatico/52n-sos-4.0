package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * ObservationConstellation generated by hbm2java
 */
public class ObservationConstellation implements java.io.Serializable {

	private long observationConstellationId;
	private ObservationType observationType;
	private ObservableProperty observableProperty;
	private Procedure procedure;
	private Offering offering;
	private ResultType resultType;
	private Set resultTemplates = new HashSet(0);
	private Set observations = new HashSet(0);

	public ObservationConstellation() {
	}

	public ObservationConstellation(long observationConstellationId,
			ObservationType observationType,
			ObservableProperty observableProperty, Procedure procedure,
			Offering offering, ResultType resultType) {
		this.observationConstellationId = observationConstellationId;
		this.observationType = observationType;
		this.observableProperty = observableProperty;
		this.procedure = procedure;
		this.offering = offering;
		this.resultType = resultType;
	}

	public ObservationConstellation(long observationConstellationId,
			ObservationType observationType,
			ObservableProperty observableProperty, Procedure procedure,
			Offering offering, ResultType resultType, Set resultTemplates,
			Set observations) {
		this.observationConstellationId = observationConstellationId;
		this.observationType = observationType;
		this.observableProperty = observableProperty;
		this.procedure = procedure;
		this.offering = offering;
		this.resultType = resultType;
		this.resultTemplates = resultTemplates;
		this.observations = observations;
	}

	public long getObservationConstellationId() {
		return this.observationConstellationId;
	}

	public void setObservationConstellationId(long observationConstellationId) {
		this.observationConstellationId = observationConstellationId;
	}

	public ObservationType getObservationType() {
		return this.observationType;
	}

	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}

	public ObservableProperty getObservableProperty() {
		return this.observableProperty;
	}

	public void setObservableProperty(ObservableProperty observableProperty) {
		this.observableProperty = observableProperty;
	}

	public Procedure getProcedure() {
		return this.procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Offering getOffering() {
		return this.offering;
	}

	public void setOffering(Offering offering) {
		this.offering = offering;
	}

	public ResultType getResultType() {
		return this.resultType;
	}

	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}

	public Set getResultTemplates() {
		return this.resultTemplates;
	}

	public void setResultTemplates(Set resultTemplates) {
		this.resultTemplates = resultTemplates;
	}

	public Set getObservations() {
		return this.observations;
	}

	public void setObservations(Set observations) {
		this.observations = observations;
	}

}
