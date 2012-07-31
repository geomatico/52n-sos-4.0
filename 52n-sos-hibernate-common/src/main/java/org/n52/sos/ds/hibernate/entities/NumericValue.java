package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * NumericValue generated by hbm2java
 */
public class NumericValue implements java.io.Serializable {

	private long numericValueId;
	private double value;
	private Set observations = new HashSet(0);

	public NumericValue() {
	}

	public NumericValue(long numericValueId, double value) {
		this.numericValueId = numericValueId;
		this.value = value;
	}

	public NumericValue(long numericValueId, double value, Set observations) {
		this.numericValueId = numericValueId;
		this.value = value;
		this.observations = observations;
	}

	public long getNumericValueId() {
		return this.numericValueId;
	}

	public void setNumericValueId(long numericValueId) {
		this.numericValueId = numericValueId;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Set getObservations() {
		return this.observations;
	}

	public void setObservations(Set observations) {
		this.observations = observations;
	}

}
