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
package org.n52.sos.service;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Thread safe factory that creates a object only if
 * it is null or if it should be recreated explictly.
 * @param <T> the type to produce
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class LazyThreadSafeFactory<T> {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private T object;
	private boolean recreate = false;

	private void recreate() throws OwsExceptionReport {
		this.object = create();
		this.recreate = false;
	}

	private boolean shouldRecreate() {
		return this.object == null || recreate;
	}

	protected void setRecreate() {
		lock.writeLock().lock();
		try {
			this.recreate = true;
		} finally {
			lock.writeLock().unlock();
		}
	}

	protected T get() throws OwsExceptionReport {
		lock.readLock().lock();
		try {
			if (!shouldRecreate()) {
				return this.object;
			}
		} finally {
			lock.readLock().unlock();
		}

		lock.writeLock().lock();
		try {
			if (shouldRecreate()) {
				recreate();
			}
			return this.object;
		} finally {
			lock.writeLock().unlock();
		}
	}

	protected abstract T create() throws OwsExceptionReport;
}
