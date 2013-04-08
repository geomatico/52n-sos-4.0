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

package org.n52.sos.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.n52.sos.cache.ctrl.CacheUpdate;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.GroupedAndNamedThreadFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class UpdateSchedulingTest extends AbstractCacheControllerTest {

    private static final long TIMEOUT = 1000 * 5;
    private static final long PAUSE = 500;

    @Test
    public void test() throws InterruptedException {
        final TestableInMemoryCacheController ue = new TestableInMemoryCacheController();
        ue.setCache(new WritableCache());
        ExecutorService e = Executors.newFixedThreadPool(10, new GroupedAndNamedThreadFactory("test"));

        e.execute(new BlockingCacheUpdate(ue, "complete0", TIMEOUT));
        Thread.sleep(PAUSE);
        e.execute(new NonBlockingCacheUpdate(ue, "partial1"));
        e.execute(new BlockingCacheUpdate(ue, "complete2", TIMEOUT));
        e.execute(new NonBlockingCacheUpdate(ue, "partial2"));
        e.execute(new BlockingCacheUpdate(ue, "complete3", TIMEOUT));
        e.execute(new NonBlockingCacheUpdate(ue, "partial3"));
        Thread.sleep(TIMEOUT);
        e.execute(new BlockingCacheUpdate(ue, "complete4", TIMEOUT));
        Thread.sleep(TIMEOUT);
        e.execute(new NonBlockingCacheUpdate(ue, "partial4"));
        /*
         * expected: 1, TIMEOUT, 2,3, TIMEOUT 4
         */
        e.shutdown();
        e.awaitTermination(TIMEOUT * 10, TimeUnit.MILLISECONDS);
    }

    private class BlockingCacheUpdate extends NonBlockingCacheUpdate {
        private final long timeout;

        BlockingCacheUpdate(TestableInMemoryCacheController e, String offering, long timeout) {
            super(e, offering);
            this.timeout = timeout;
        }

        @Override
        public void execute() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                fail(new GenericThrowableWrapperException(ex));
            }
            super.execute();
        }

        @Override
        public boolean isCompleteUpdate() {
            return true;
        }
    }

    private class NonBlockingCacheUpdate extends CacheUpdate implements Runnable {
        private final TestableInMemoryCacheController executor;
        private final String offering;

        NonBlockingCacheUpdate(TestableInMemoryCacheController executor, String offering) {
            this.offering = offering;
            this.executor = executor;
        }

        @Override
        public void execute() {
            getCache().addOffering(offering);
        }

        @Override
        public boolean isCompleteUpdate() {
            return false;
        }

        @Override
        public void run() {
            try {
                executor.update(this);
            } catch (OwsExceptionReport ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String toString() {
            return String.format("%s[offering=%s]", getClass().getSimpleName(), offering);
        }
    }
}
