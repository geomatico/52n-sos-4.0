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
package org.n52.sos.encode;

import java.util.Set;

import org.n52.sos.config.SettingsManager;

public interface ObservationEncoder<S, T> extends Encoder<S, T> {
    
    public boolean isObservationAndMeasurmentV20Type();
    
    public boolean shouldObservationsWithSameXBeMerged();
    
    /**
     * Indicates, if this {@link ObservationEncoder} is currently activated in the SOS

     *
     * @return
     *
     * @deprecated see {@link SettingsManager#isActive(org.n52.sos.encode.ResponseFormatKeyType)} and
     * {@link SettingsManager#setActive(org.n52.sos.encode.ResponseFormatKeyType, boolean)}.
     */
    @Deprecated
    public boolean isSupported();

    @Deprecated
    public void setSupported(boolean supported);
    
    // TODO add javadoc
    public Set<String> getSupportedResponseFormats(String service, String version);

}
