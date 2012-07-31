package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/**
 * GeometryValue generated by hbm2java
 */
public class GeometryValue implements java.io.Serializable {

	private long geometryValueId;
	private Geometry value;
	private Set observations = new HashSet(0);

	public GeometryValue() {
	}

	public GeometryValue(long geometryValueId, Geometry value) {
		this.geometryValueId = geometryValueId;
		this.value = value;
	}

	public GeometryValue(long geometryValueId, Geometry value,
			Set observations) {
		this.geometryValueId = geometryValueId;
		this.value = value;
		this.observations = observations;
	}

	public long getGeometryValueId() {
		return this.geometryValueId;
	}

	public void setGeometryValueId(long geometryValueId) {
		this.geometryValueId = geometryValueId;
	}

	public Geometry getValue() {
		return this.value;
	}

	public void setValue(Geometry value) {
		this.value = value;
	}

	public Set getObservations() {
		return this.observations;
	}

	public void setObservations(Set observations) {
		this.observations = observations;
	}

}
