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
package org.n52.sos.ogc.om.features.samplingFeatures;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.n52.sos.exception.ows.concrete.InvalidSridException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.util.CollectionHelper;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Abstract super class for all sampling features
 * 
 * 
 */
public class SamplingFeature extends AbstractFeature {

    private static final long serialVersionUID = 4660755526492323288L;

    private final List<CodeType> name = new LinkedList<CodeType>();

    private String description;

    private String xmlDescription;

    private Geometry geometry;

    private String featureType = OGCConstants.UNKNOWN;

    private String url;

    private final List<AbstractFeature> sampledFeatures = new LinkedList<AbstractFeature>();

    private final List<NamedValue<?>> parameters = new LinkedList<NamedValue<?>>();

    private boolean encode = true;

	private Collection<SamplingFeatureComplex> relatedSamplingFeatures;

    /**
     * constructor
     * 
     * @param featureIdentifier
     *            identifier of sampling feature
     */
    public SamplingFeature(final CodeWithAuthority featureIdentifier) {
        super(featureIdentifier);
    }

    public SamplingFeature(final CodeWithAuthority featureIdentifier, final String gmlId) {
        super(featureIdentifier, gmlId);
    }

    public List<CodeType> getName() {
        return Collections.unmodifiableList(name);
    }

    public void setName(final List<CodeType> name) {
        this.name.addAll(name);
    }

    public void addName(final CodeType name) {
        this.name.add(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getXmlDescription() {
        return xmlDescription;
    }

    public boolean isSetXmlDescription() {
        return xmlDescription != null && !xmlDescription.isEmpty();
    }

    public void setXmlDescription(final String xmlDescription) {
        this.xmlDescription = xmlDescription;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final Geometry geometry) throws InvalidSridException {
        if (geometry != null && geometry.getSRID() == 0) {
            throw new InvalidSridException(0);
        }
        this.geometry = geometry;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(final String featureType) {
        this.featureType = featureType;
    }

    public boolean isSetFeatureType() {
        return featureType != null && !featureType.isEmpty();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setSampledFeatures(final List<AbstractFeature> sampledFeatures) {
        this.sampledFeatures.addAll(sampledFeatures);
    }

    public List<AbstractFeature> getSampledFeatures() {
        if (isSetSampledFeatures()) {
            return Collections.unmodifiableList(sampledFeatures);
        }
        return Collections.emptyList();
    }

    public boolean isSetNames() {
        return CollectionHelper.isNotEmpty(name);
    }

    public CodeType getFirstName() {
        if (isSetNames()) {
            return name.get(0);
        }
        return null;
    }

    public boolean isSetSampledFeatures() {
        return CollectionHelper.isNotEmpty(sampledFeatures);
    }

    public boolean isSetUrl() {
        return url != null && !url.isEmpty();
    }

    public boolean isSetGeometry() {
        return geometry != null && !geometry.isEmpty();
    }

    public void addParameter(final NamedValue<?> namedValue) {
        parameters.add(namedValue);
    }
    
    public void setParameters(final List<NamedValue<?>> parameters) {
        this.parameters.addAll(parameters);
    }

    public List<NamedValue<?>> getParameters() {
        return parameters;
    }

    public boolean isSetParameter() {
        return CollectionHelper.isNotEmpty(parameters);
    }

    public boolean isEncode() {
        return encode;
    }

    public void setEncode(final boolean encode) {
        this.encode = encode;
    }

	public void addRelatedSamplingFeature(final SamplingFeatureComplex relatedSamplingFeature)
	{
		if (!isSetRelatedSamplingFeatures()) {
			relatedSamplingFeatures = CollectionHelper.set();
		}
		if (relatedSamplingFeature != null) {
			relatedSamplingFeatures.add(relatedSamplingFeature);
		}
	}
	
	public void addAllRelatedSamplingFeatures(final Collection<SamplingFeatureComplex> relatedSamplingFeatures) {
		if (isSetRelatedSamplingFeatures()) {
			this.relatedSamplingFeatures.addAll(relatedSamplingFeatures);
		} else {
			this.relatedSamplingFeatures = relatedSamplingFeatures;
		}
	}
	
	public void setRelatedSamplingFeatures(final Collection<SamplingFeatureComplex> relatedSamplingFeatures) {
		this.relatedSamplingFeatures = relatedSamplingFeatures;
	}
	
	public List<SamplingFeatureComplex> getRelatedSamplingFeatures(){
		return CollectionHelper.asList(relatedSamplingFeatures);
	}
	
	public boolean isSetRelatedSamplingFeatures() {
		return CollectionHelper.isNotEmpty(relatedSamplingFeatures);
	}

	@Override
	public String toString()
	{
		return String.format(
				"SamplingFeature [name=%s, description=%s, xmlDescription=%s, geometry=%s, featureType=%s, url=%s, sampledFeatures=%s, parameters=%s, encode=%s, relatedSamplingFeatures=%s]", name,
				description, xmlDescription, geometry, featureType, url, sampledFeatures, parameters, encode, relatedSamplingFeatures);
	}

}
