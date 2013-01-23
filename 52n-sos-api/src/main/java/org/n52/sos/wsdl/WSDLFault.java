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
package org.n52.sos.wsdl;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import org.n52.sos.util.CollectionHelper;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLFault {
    public static final WSDLFault REQUEST_EXTENSION_NOT_SUPPORTED_EXCEPTION = new WSDLFault("RequestExtensionNotSupportedException", WSDLConstants.SWES_EXCEPTION_ACTION);
    public static final WSDLFault INVALID_REQUEST_EXCEPTION = new WSDLFault("InvalidRequestException", WSDLConstants.SWES_EXCEPTION_ACTION);
    public static final WSDLFault NO_APPLICABLE_CODE_EXCEPTION = new WSDLFault("NoApplicableCodeException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final WSDLFault INVALID_UPDATE_SEQUENCE_EXCEPTION = new WSDLFault("InvalidUpdateSequenceException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final WSDLFault VERSION_NEGOTIATION_FAILED_EXCEPTION = new WSDLFault("VersionNegotiationFailedException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final WSDLFault MISSING_PARAMETER_VALUE_EXCEPTION = new WSDLFault("MissingParameterValueException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final WSDLFault INVALID_PARAMETER_VALUE_EXCEPTION = new WSDLFault("InvalidParameterValueException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final WSDLFault OPERATION_NOT_SUPPORTED_EXCEPTION = new WSDLFault("OperationNotSupportedException", WSDLConstants.OWS_EXCEPTION_ACTION);
    public static final Collection<WSDLFault> DEFAULT_FAULTS = Collections.unmodifiableCollection(CollectionHelper.collection(
        WSDLFault.MISSING_PARAMETER_VALUE_EXCEPTION,
        WSDLFault.INVALID_PARAMETER_VALUE_EXCEPTION,
        WSDLFault.OPERATION_NOT_SUPPORTED_EXCEPTION,
        WSDLFault.NO_APPLICABLE_CODE_EXCEPTION,
        WSDLFault.INVALID_REQUEST_EXCEPTION,
        WSDLFault.REQUEST_EXTENSION_NOT_SUPPORTED_EXCEPTION
    ));
    private final String name;
    private final URI action;

    public WSDLFault(String name, URI action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public URI getAction() {
        return action;
    }
}
