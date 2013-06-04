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

package org.n52.sos.service.it;

import org.junit.Test;
import org.n52.sos.ogc.sos.SosConstants;

/**
 * Abstract class for SOS KVP tests
 * 
 * @author Christian Autermann <c.autermann@52north.org>
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.0
 */
public abstract class AbstractKvpTest extends AbstractSosServiceTest {

    protected RequestBuilder builder() {
        return RequestBuilder.get("/kvp").accept(SosConstants.CONTENT_TYPE_XML);
    }

    /**
     * Definition for missing service parameter
     */
    @Test
    public abstract void missingServiceParameter();

    /**
     * Definition for empty service parameter value
     */
    @Test
    public abstract void emptyServiceParameter();

    /**
     * Definition for invalid service parameter value
     */
    @Test
    public abstract void invalidServiceParameter();

    /**
     * Definition for missing version parameter
     */
    @Test
    public abstract void missingVersionParameter();

    /**
     * Definition for empty version parameter value
     */
    @Test
    public abstract void emptyVersionParameter();

    /**
     * Definition for invalid version parameter value
     */
    @Test
    public abstract void invalidVersionParameter();

}
