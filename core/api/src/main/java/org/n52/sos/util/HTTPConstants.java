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

package org.n52.sos.util;

/**
 * TODO JavaDoc
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public interface HTTPConstants {
    enum StatusCode {
    	BAD_REQUEST(400),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        INTERNAL_SERVER_ERROR(500) 
        ;
    	
        private final int code;

        private StatusCode(final int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
