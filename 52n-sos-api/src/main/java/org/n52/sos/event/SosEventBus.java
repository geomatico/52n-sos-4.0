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
package org.n52.sos.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.n52.sos.util.ClassHelper;
import org.n52.sos.util.GroupedAndNamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SosEventBus {
    private static final Logger log = LoggerFactory.getLogger(SosEventBus.class);
    private static final boolean ASYNCHRONOUS_EXECUTION = false;
    private static final int THREAD_POOL_SIZE = 3;
    private static final String THREAD_GROUP_NAME = "SosEventBus-Worker";
    private static final Object SINGLETON_CREATION_LOCK = new Object();

    private static SosEventBus instance;
    
	public static SosEventBus getInstance() {
		if (instance == null) {
			synchronized (SINGLETON_CREATION_LOCK) {
				if (instance == null) {
					instance = new SosEventBus();
				}
			}
		}
		return instance;
	}
    
    public static void fire(SosEvent event) {
        getInstance().submit(event);
    }

    private static boolean checkEvent(SosEvent event) {
        if (event == null) {
            log.warn("Submitted event is null!");
            return false;
		}
        return true;
	}

    private static boolean checkListener(SosEventListener listener) {
        if (listener == null) {
            log.warn("Tried to unregister SosEventListener null");
            return false;
        }
        if (listener.getTypes() == null || listener.getTypes().isEmpty()) {
            log.warn("Listener {} has no EventTypes", listener);
            return false;
        }
        return true;
    }
    private final ClassCache classCache = new ClassCache();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Executor executor = Executors
            .newFixedThreadPool(THREAD_POOL_SIZE, new GroupedAndNamedThreadFactory(THREAD_GROUP_NAME));
    private final Map<Class<? extends SosEvent>, Set<SosEventListener>> listeners =
                                                                        new HashMap<Class<? extends SosEvent>, Set<SosEventListener>>();
    private final Queue<HandlerExecution> queue = new ConcurrentLinkedQueue<HandlerExecution>();

    private SosEventBus() {
        loadListenerImplementations();
    }

    private void loadListenerImplementations() {
        ServiceLoader<SosEventListener> serviceLoader = ServiceLoader.load(SosEventListener.class);
        Iterator<SosEventListener> iter = serviceLoader.iterator();
        while (iter.hasNext()) {
            try {
                register(iter.next());
            } catch (ServiceConfigurationError e) {
                log.error("Could not load Listener implementation", e);
            }
        }
    }

    private Set<SosEventListener> getListenersForEvent(SosEvent event) {
        LinkedList<SosEventListener> result = new LinkedList<SosEventListener>();
        this.lock.readLock().lock();
        try {
            for (Class<? extends SosEvent> eventType : this.classCache.getClasses(event.getClass())) {
                Set<SosEventListener> listenersForClass = this.listeners.get(eventType);
                
                if (listenersForClass != null) {
                    log
                            .trace("Adding {} Listeners for event {} (eventType={})", listenersForClass.size(), event, eventType);
                    result.addAll(listenersForClass);
                } else {
                    log.trace("Adding 0 Listeners for event {} (eventType={})", event, eventType);
                }
                
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return new HashSet<SosEventListener>(result);
    }

	public void submit(SosEvent event) {
        boolean submittedEvent = false;
        if (!checkEvent(event)) {
            return;
        }
        this.lock.readLock().lock();
        try {
            for (SosEventListener listener : getListenersForEvent(event)) {
                submittedEvent = true;
                log.debug("Queueing Event {} for Listener {}", event, listener);
                this.queue.offer(new HandlerExecution(event, listener));
            }
        } finally {
            this.lock.readLock().unlock();
        }
        HandlerExecution r;
        while ((r = this.queue.poll()) != null) {
            if (ASYNCHRONOUS_EXECUTION) {
                this.executor.execute(r);
            } else {
                r.run();
            }
        }
        if (!submittedEvent) {
            log.info("No Listeners for SosEvent {}", event);
        }
    }

	public void register(SosEventListener listener) {
        if (!checkListener(listener)) {
            return;
        }
        this.lock.writeLock().lock();
        try {
            for (Class<? extends SosEvent> eventType : listener.getTypes()) {
                log.debug("Subscibing Listener {} to EventType {}", listener, eventType);
                Set<SosEventListener> listenersForKey = this.listeners.get(eventType);
                if (listenersForKey == null) {
                    this.listeners.put(eventType, listenersForKey = new HashSet<SosEventListener>());
                }
                listenersForKey.add(listener);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

	public void unregister(SosEventListener listener) {
        if (!checkListener(listener)) {
            return;
        }
        this.lock.writeLock().lock();
        try {
            for (Class<? extends SosEvent> eventType : listener.getTypes()) {
                Set<SosEventListener> listenersForKey = this.listeners.get(eventType);
                if (listenersForKey.contains(listener)) {
                    log.debug("Unsubscibing Listener {} from EventType {}", listener, eventType);
                    listenersForKey.remove(listener);
                } else {
                    log.warn("Listener {} was not registered for SosEvent Type {}", listener, eventType);
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

	private class ClassCache {
		private ReadWriteLock lock = new ReentrantReadWriteLock();
		private Map<Class<? extends SosEvent>, Set<Class<? extends SosEvent>>> cache
				= new HashMap<Class<? extends SosEvent>, Set<Class<? extends SosEvent>>>();

		public Set<Class<? extends SosEvent>> getClasses(Class<? extends SosEvent> eventClass)  {
			lock.readLock().lock();
			try {
				Set<Class<? extends SosEvent>> r = cache.get(eventClass);
				if (r != null) { return r; }
			} finally {
				lock.readLock().unlock();
			}
			lock.writeLock().lock();
			try {
				Set<Class<? extends SosEvent>> r = cache.get(eventClass);
				if (r != null) { return r; }
				cache.put(eventClass, r = flatten(eventClass));
				return r;
			} finally {
				lock.writeLock().unlock();
			}
		}

		private Set<Class<? extends SosEvent>> flatten(Class<? extends SosEvent> eventClass) {
			return ClassHelper.flattenPartialHierachy(SosEvent.class, eventClass);
		}
	}
    
	private class HandlerExecution implements Runnable {
		private SosEvent event;
		private SosEventListener listener;

		HandlerExecution(SosEvent event, SosEventListener listener) {
			this.event = event;
			this.listener = listener;
		}

		@Override public void run() {
			try {
				log.debug("Submitting Event {} to Listener {}", event, listener);
				listener.handle(event);
			} catch (Throwable t) {
				log.error(String.format("Error handling event %s by handler %s", event, listener), t);
			}
		}
	}
}
