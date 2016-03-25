/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.utils;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The eXo Platform SAS
 * Author : Aymen Boughzela
 *          aboughzela@exoplatform.com
 */
public class DateUtils {
    /**
     * registered time zones.
     */
    private static volatile Map<String, TimeZone> TIME_ZONES = new ConcurrentHashMap<String, TimeZone>();


    /**
     * This method is similar to {@link TimeZone#getTimeZone(String)} with less contention
     */
    public static TimeZone getTimeZone(String ID) {
        if (ID == null) {
            throw new IllegalArgumentException("ID of the timezone cannot be null");
        }

        TimeZone tz = TIME_ZONES.get(ID);
        if (tz == null) {
            tz = TimeZone.getTimeZone(ID);
            TIME_ZONES.put(ID, tz);
        }
        return tz;
    }
}
