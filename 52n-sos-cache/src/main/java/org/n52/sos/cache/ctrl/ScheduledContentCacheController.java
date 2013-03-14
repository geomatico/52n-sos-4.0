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
package org.n52.sos.cache.ctrl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.DateTime;
import org.n52.sos.cache.ContentCacheController;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for capabilities cache controller implementations that schedules a complete cache update at a
 * configured interval.
 */
@Configurable
public abstract class ScheduledContentCacheController implements ContentCacheController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledContentCacheController.class);
    private final ReentrantLock updateLock = new ReentrantLock(true);
    private final Condition updateFree = updateLock.newCondition();
    private boolean initialized = false;
    private boolean updateIsFree = true;
    private long updateInterval;
	private final Timer timer = new Timer("52n-sos-capabilities-cache-controller", true);
	private TimerTask current = null;

	/**
	 * Starts a new timer task
	 */
    private void schedule() {
		/*
		 * Timers can not be rescheduled.
		 * To make the interval changeable reschedule a new timer.
		 */
		current = new UpdateTimerTask();
		long delay = getUpdateInterval();
		if (!isInitialized()) {
			delay = 1;
			setInitialized(true);
		}
		if (delay > 0) {
			LOGGER.info("Next CapabilitiesCacheUpdate in {}m: {}", delay/60000, new DateTime(System.currentTimeMillis()+delay));
			timer.schedule(current, delay);
		}
	}

    @Setting(ScheduledContentCacheControllerSettings.CAPABILITIES_CACHE_UPDATE_INTERVAL)
    public void setUpdateInterval(int interval) throws ConfigurationException {
        Validation.greaterZero("Cache update interval", interval);
        if (this.updateInterval != interval) {
            this.updateInterval = interval;
            reschedule();
        }
    }

    private long getUpdateInterval() {
        return this.updateInterval * 60000;
    }

	/**
	 * Stops the current task, if available and starts a new {@link TimerTask}.
	 * @see #schedule()
	 */
    private void reschedule() {
        cancelCurrent();
		schedule();
	}
    
    private void cancelCurrent() {
        if (this.current != null) {
            this.current.cancel();
            LOGGER.debug("Current {} canceled", UpdateTimerTask.class.getSimpleName());
        }
    }
    
    private void cancelTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            LOGGER.debug("Cache Update timer canceled.");
        }
    }

    @Override
	public void cleanup() {
        cancelCurrent(); 
        cancelTimer();
	}

    @Override
    protected void finalize() {
        try {
			cleanup();
            super.finalize();
        } catch (Throwable e) {
            LOGGER.error("Could not finalize CapabilitiesCacheController! " + e.getMessage(), e);
        }
    }

    /**
	 * @return the updateIsFree
	 */
	protected boolean isUpdateIsFree() {
	    return updateIsFree;
	}


	/**
	 * @param updateIsFree
	 *            the updateIsFree to set
	 */
	protected void setUpdateIsFree(boolean updateIsFree) {
        this.updateIsFree = updateIsFree;
	}


	/**
	 * @return the updateLock
	 */
	protected ReentrantLock getUpdateLock() {
	    return updateLock;
	}


	/**
	 * @return the updateFree
	 */
    protected Condition getUpdateFree() {
	    return updateFree;
	}

    /**
     * @return the initialized
     */
    protected boolean isInitialized() {
        return initialized;
    }

    /**
     * @param initialized the initialized to set
     */
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (updateCacheFromDatasource()) {
                    LOGGER.info("Timertask: capabilities cache update successful!");
                } else {
                    LOGGER.warn("Timertask: capabilities cache update not successful!");
                }
                schedule();
            } catch (OwsExceptionReport e) {
                LOGGER
                        .error("Fatal error: Timertask couldn't update capabilities cache! Switch log level to DEBUG to get more details.");
                LOGGER.debug("Exception thrown", e);
            }
        }
    }
}
