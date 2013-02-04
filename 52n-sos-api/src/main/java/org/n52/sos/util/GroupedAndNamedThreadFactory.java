/*
 * Copyright (C) 2013 52north.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.n52.sos.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class GroupedAndNamedThreadFactory implements ThreadFactory {
    private final AtomicInteger i = new AtomicInteger(0);
    private final ThreadGroup tg ;

    public GroupedAndNamedThreadFactory(String name) {
        tg = new ThreadGroup(name);
    }
    
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(tg, r, String.format("%s-%d", tg.getName(), i.getAndIncrement()));
    }
    
}
