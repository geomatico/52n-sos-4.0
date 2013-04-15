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
package org.n52.sos.util;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ClassHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClassHelper.class);
    
    /**
     * Calculates class similarity based on hierarchy depth.
     * 
     * @param superClass
     * @param clazz
     * @return 0 for equality, -1 for non-hierarchy classes, >0 the lower the more similiar
     */
    public static int getSimiliarity(Class<?> superClass, Class<?> clazz) {
        if (clazz.isArray()) {
            if (!superClass.isArray()) {
                return -1;
            } else {
                return getSimiliarity(superClass.getComponentType(), clazz.getComponentType());
            }
        }
        if (superClass == clazz) {
            return 0;
        } else {
            
            int difference = -1;
            if (clazz.getSuperclass() != null) {
                difference = getSimiliarity1(superClass, clazz.getSuperclass(), -1);
            }
            if (difference != 0 && superClass.isInterface()) {
                for (Class<?> i : clazz.getInterfaces()) {
                    difference = getSimiliarity1(superClass, i, difference);
                    if (difference == 0) {
                        break;
                    }
                }
            }
            return difference < 0 ? -1 : 1 + difference;
        }
    }

    private static int getSimiliarity1(Class<?> superClass, Class<?> clazz, int difference) {
        if (superClass.isAssignableFrom(clazz)) {
            int cd = getSimiliarity(superClass, clazz);
            return (cd >= 0) ? ((difference < 0) ? cd : Math.min(difference, cd)) : difference;
        } else {
            return difference;
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> Set<Class<? extends T>> flattenPartialHierachy(Set<Class<? extends T>> alreadyFoundClasses, Class<T> limitingClass, Class<?> currentClass) {
        if (limitingClass.isAssignableFrom(currentClass)) {
            alreadyFoundClasses.add((Class<? extends T>) currentClass);
            if (limitingClass.isInterface()) {
                for (Class<?> c : currentClass.getInterfaces()) {
                    if (limitingClass.isAssignableFrom(c)) {
                        alreadyFoundClasses.add((Class<? extends T>) c);
                    }
                }
            }
            Class<?> superClass = currentClass.getSuperclass();
            if (superClass != null) {
                return flattenPartialHierachy(alreadyFoundClasses, limitingClass, (Class<?>) currentClass.getSuperclass());
            } else {
                return alreadyFoundClasses;
            }
        } else {
            return alreadyFoundClasses;
        }
    }

    public static <T> Set<Class<? extends T>> flattenPartialHierachy(Class<T> limitingClass, Class<? extends T> actualClass) {
        Set<Class<? extends T>> classes = flattenPartialHierachy(new HashSet<Class<? extends T>>(), limitingClass, actualClass);
        LOG.debug("Flatten class hierarchy for {} extending/implementing {}; Found: {}", actualClass, limitingClass, StringHelper.join(", ", classes));
        return classes;
    }

    private ClassHelper() {
    }
}
