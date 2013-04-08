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

package org.n52.sos.cache.ctrl.action;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.cache.ctrl.CacheFactory;
import org.n52.sos.ds.CacheFeederDAO;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CompleteCacheUpdate extends DatasourceCacheUpdate {
    public CompleteCacheUpdate(CacheFeederDAO dao) {
        super(dao);
    }

    @Override
    public void execute() {
        try {
            WritableContentCache cache = CacheFactory.getInstance().create();
            getDao().updateCache(cache);
            setCache(cache);
        } catch (OwsExceptionReport ex) {
            fail(ex);
        }
    }

    @Override
    public boolean isCompleteUpdate() {
        return true;
    }
}
