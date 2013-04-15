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
package org.n52.sos.tasking;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Timer;

import org.n52.sos.util.Cleanupable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Tasking implements Cleanupable {

	private static final Logger LOG = LoggerFactory.getLogger(Tasking.class);
	private final ServiceLoader<ASosTasking> serviceLoader = ServiceLoader.load(ASosTasking.class);
	private Timer taskingExecutor;

	public Tasking() {
		load();
	}

    @Override
	public void cleanup() {
		if (taskingExecutor != null) {
			taskingExecutor.cancel();
			taskingExecutor = null;
		}
	}

	private void load() {
		Iterator<ASosTasking> iterator = this.serviceLoader.iterator();
		if (iterator.hasNext()) {
			this.taskingExecutor = new Timer("TaskingTimer");
			long delayCounter = 0;
			while (iterator.hasNext()) {
				try {
					ASosTasking aSosTasking = iterator.next();
					this.taskingExecutor.scheduleAtFixedRate(aSosTasking, delayCounter,
							(aSosTasking.getExecutionIntervall() * 60000));
					delayCounter += 60000;
					LOG.debug("The task '{}' is started!", aSosTasking.getName());
				} catch (Exception e) {
					LOG.error("Error while starting task", e);
				}
			}
			LOG.info("\n******\n Task(s) loaded and started successfully!\n******\n");
		}
	}
}
