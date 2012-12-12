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
package org.n52.sos.ogc.swe;

import java.util.ArrayList;
import java.util.List;

public class RangeValue<T> {
    
    private T rangeStart;
    
    private T rangeEnd;

    public T getRangeStart() {
        return rangeStart;
    }

    public T getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeStart(T rangeStart) {
        this.rangeStart = rangeStart;
    }

    public void setRangeEnd(T rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
    
    public boolean isSetStartValue() {
        return rangeStart != null;
    }
    
    public boolean isSetEndValue() {
        return rangeEnd != null;
    }

    public List<T> getRangeAsList() {
        List<T> list = new ArrayList<T>();
        list.add(rangeStart);
        list.add(rangeEnd);
        return list;
    }
    
    public List<String> getRangeAsStringList() {
        List<String> list = new ArrayList<String>();
        list.add(rangeStart.toString());
        list.add(rangeEnd.toString());
        return list;
    }
}
