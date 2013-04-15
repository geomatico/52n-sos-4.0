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
package org.n52.sos.ds.hibernate.cache;

import org.n52.sos.util.CompositeAction;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class CompositeCacheUpdate extends AbstractDatasourceCacheUpdate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeCacheUpdate.class);

    private CompositeAction<AbstractDatasourceCacheUpdate> delegatedAction;

    public CompositeCacheUpdate(AbstractDatasourceCacheUpdate... actions) {
        this.delegatedAction = new CompositeAction<AbstractDatasourceCacheUpdate>(actions) {
            @Override protected void pre(AbstractDatasourceCacheUpdate action) {
                action.setCache(getCache());
                action.setErrors(getErrors());
                action.setSession(getSession());
                LOGGER.debug("Running {}.", action);
            }
            @Override protected void post(AbstractDatasourceCacheUpdate action) {
                getSession().clear();
            }
        };
    }

    @Override
    public void execute() {
        delegatedAction.execute();
    }

    @Override
    public String toString() {
        return String.format("%s [actions=[%s]]", getClass().getSimpleName(),
                StringHelper.join(", ", delegatedAction.getActions()));
    }
}
