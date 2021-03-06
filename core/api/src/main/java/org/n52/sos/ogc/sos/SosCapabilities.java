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
package org.n52.sos.ogc.sos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.n52.sos.ogc.filter.FilterCapabilities;
import org.n52.sos.ogc.ows.OwsOperationsMetadata;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.ows.SwesExtension;

/**
 * Class which represents the Capabilities.
 * 
 */
public class SosCapabilities {

    /**
     * Service identification, loaded from file.
     */
    private SosServiceIdentification serviceIdentification;

    /**
     * Service provider, loaded from file.
     */
    private SosServiceProvider serviceProvider;

    /**
     * Operations metadata for all supported operations.
     */
    private OwsOperationsMetadata operationsMetadata;

    /**
     * Metadata for all supported filter
     */
    private FilterCapabilities filterCapabilities;

    /**
     * All ObservationOfferings provided by this SOS.
     */
    private SortedSet<SosObservationOffering> contents = new TreeSet<SosObservationOffering>();

    /**
     * extensions
     */
    private List<SwesExtension> extensions = new LinkedList<SwesExtension>();

    /**
     * Set service identification
     * 
     * @param serviceIdentification
     *            service identification
     */
    public void setServiceIdentification(SosServiceIdentification serviceIdentification) {
        this.serviceIdentification = serviceIdentification;

    }

    /**
     * Get service identification
     * 
     * @return service identification
     */
    public SosServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * Set service provider
     * 
     * @param serviceProvider
     *            service provider
     */
    public void setServiceProvider(SosServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;

    }

    /**
     * Get service provider
     * 
     * @return service provider
     */
    public SosServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Get operations metadata
     * 
     * @return operations metadata
     */
    public OwsOperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

    /**
     * Set operations metadata
     * 
     * @param operationsMetadata
     *            operations metadata
     */
    public void setOperationsMetadata(OwsOperationsMetadata operationsMetadata) {
        this.operationsMetadata = operationsMetadata;
    }

    /**
     * Get filter capabilities
     * 
     * @return filter capabilities
     */
    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * Set filter capabilities
     * 
     * @param filterCapabilities
     *            filter capabilities
     */
    public void setFilterCapabilities(FilterCapabilities filterCapabilities) {
        this.filterCapabilities = filterCapabilities;
    }

    /**
     * Get contents data
     * 
     * @return contents data
     */
    public SortedSet<SosObservationOffering> getContents() {
        return Collections.unmodifiableSortedSet(contents);
    }

    /**
     * Set contents data
     * 
     * @param contents
     *            contents data
     */
    public void setContents(Collection<SosObservationOffering> contents) {
        this.contents = new TreeSet<SosObservationOffering>(contents);
    }

    /**
     * Set extension data
     * 
     * @param extensions
     *            extension data
     */
    public void setExensions(Collection<SwesExtension> extensions) {
        this.extensions = extensions == null ? new LinkedList<SwesExtension>()
                          : new ArrayList<SwesExtension>(extensions);
    }

    /**
     * Get extension data
     * 
     * @return extension data
     */
    public List<SwesExtension> getExtensions() {
        return Collections.unmodifiableList(this.extensions);
    }

    public boolean isSetContents() {
        return contents != null && !contents.isEmpty();
    }
}
