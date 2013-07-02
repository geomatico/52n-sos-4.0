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
package org.n52.sos.ds.hibernate.cache.base;

import java.util.List;

import org.n52.sos.ds.hibernate.cache.AbstractQueuingDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author Shane StClair <shane@axiomalaska.com>
 */
public class ProcedureCacheUpdate extends AbstractQueuingDatasourceCacheUpdate<Procedure> {   
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcedureCacheUpdate.class);    
    private static final String THREAD_GROUP_NAME = "procedure-cache-update";
    
    public ProcedureCacheUpdate(int threads) {
        super(threads);
    }

    @Override
    protected String getThreadGroupName() {
        return THREAD_GROUP_NAME;
    }

    @Override
    protected List<Procedure> getObjectsToQueue() {
        return new ProcedureDAO().getProcedureObjects(getSession());
    }

    @Override
    protected void queueTask(Procedure procedure) {
        if (!procedure.isDeleted()) {
            // create runnable for procedure
            Runnable task = new ProcedureCacheUpdateTask(getCountDownLatch(), getSessionFactory(), getCache(), procedure, getErrorList());
            // put runnable in executor service
            getExecutor().submit(task);
        } else {
            getCountDownLatch().countDown();
            LOGGER.debug("Procedure '{}' is deleted, latch.countDown().", procedure.getIdentifier());
        }
    }
}
