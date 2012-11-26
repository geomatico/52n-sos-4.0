/**
 * Copyright (C) 2012
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
package org.n52.sos.ogc.om.values;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.swe.SosSweDataArray;

public class SweDataArrayValue implements IValue<SosSweDataArray> {
    
    private static final long serialVersionUID = 52L;

    private SosSweDataArray value;
    
    private String unit;

    @Override
    public void setValue(SosSweDataArray value) {
        this.value = value;
    }

    @Override
    public SosSweDataArray getValue() {
        return value;
    }

    @Override
    public void setUnit(String unit) {
        // do nothing
    }

    @Override
    public String getUnit() {
        return unit;
    }
    
    /**
     * Adds the given block - a {@link List}<{@link String}> - add the end of the current list of blocks
     * @param blockOfTokensToAddAtTheEnd
     * @return <tt>true</tt> (as specified by {@link Collection#add}) <br />
     *          <tt>false</tt> if block could not be added
     */
    public boolean addBlock(List<String> blockOfTokensToAddAtTheEnd)
    {
        if (value != null)
        {
            return value.add(blockOfTokensToAddAtTheEnd);
        }
        return false;
    }
}
