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
package org.n52.sos.exception.ows.concrete;

import static org.n52.sos.util.HTTPConstants.StatusCode.INTERNAL_SERVER_ERROR;

import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.util.HTTPConstants;
import org.n52.sos.util.StringHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class NotYetSupportedException extends NoApplicableCodeException {
    private static final long serialVersionUID = 8214490617892996058L;
    
    private final HTTPConstants.StatusCode status = INTERNAL_SERVER_ERROR;

    public NotYetSupportedException() {
        setStatus(status);
    }

    public NotYetSupportedException(final String feature) {
        withMessage("%s is not yet supported", feature);
        setStatus(status);
    }

    public NotYetSupportedException(final String type, final Object feature) {
        withMessage("The %s %s is not yet supported", type, feature);
        setStatus(status);
    }

    public NotYetSupportedException(final String type, final Object feature, final Object... supportedFeatures) {
        withMessage("The %s %s is not yet supported. Currently supported: %s",
                    type, feature, StringHelper.join(", ", supportedFeatures));
        setStatus(status);
    }
}
