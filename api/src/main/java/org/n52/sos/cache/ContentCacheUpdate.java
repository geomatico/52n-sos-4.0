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

package org.n52.sos.cache;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.Action;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class ContentCacheUpdate implements Action {
    private WritableContentCache cache;
    private OwsExceptionReport exceptionReport;

    /**
     * @return the cause of failure or {@code null}
     */
    public OwsExceptionReport getFailureCause() {
        return this.exceptionReport;
    }

    /**
     * Marks this cache update as failed.
     *
     * @param exceptionReport the cause
     *
     * @return this
     */
    protected ContentCacheUpdate fail(OwsExceptionReport exceptionReport) {
        this.exceptionReport = exceptionReport;
        return this;
    }

    /**
     * @return if this update failed
     */
    public boolean failed() {
        return this.exceptionReport != null;
    }

    /**
     * @return the writable cache of this action
     */
    public WritableContentCache getCache() {
        return cache;
    }

    /**
     * @param cache the writable cache for this action
     *
     * @return this
     */
    public ContentCacheUpdate setCache(WritableContentCache cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Clear any exceptions.
     *
     * @return this
     */
    public ContentCacheUpdate reset() {
        this.exceptionReport = null;
        return this;
    }

    /**
     * @return if this a complete update that will replace the cache.
     */
    public boolean isCompleteUpdate() {
        return false;
    }

}
