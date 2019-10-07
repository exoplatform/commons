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

import org.exoplatform.services.resources.ResourceBundleService;
import java.util.*;
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

    public static final String COMMONS_RESOUCE_BUNDLE_NAME = "locale.commons.Commons";

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

    /**
     * Gets prettyTime by timestamp.
     *
     * @param locale
     * @param postedTime
     * @return String
     */
    public static String getRelativeTimeLabel(Locale locale, long postedTime) {
        ResourceBundleService rs = CommonsUtils.getService(ResourceBundleService.class);
        ResourceBundle resourceBundle = rs.getResourceBundle(COMMONS_RESOUCE_BUNDLE_NAME, locale);
        long time = (System.currentTimeMillis() - postedTime) / 1000;
        long value;
        if (time < 60) {
            return resourceBundle.getString("TimeConvert.label.Less_Than_A_Minute");
        } else {
            if (time < 120) {
                return resourceBundle.getString("TimeConvert.label.About_A_Minute");
            } else {
                if (time < 3600) {
                    value = Math.round(time / 60);
                    return resourceBundle.getString("TimeConvert.label.About_?_Minutes").replaceFirst("\\{0\\}", String.valueOf(value));
                } else {
                    if (time < 7200) {
                        return resourceBundle.getString("TimeConvert.label.About_An_Hour");
                    } else {
                        if (time < 86400) {
                            value = Math.round(time / 3600);
                            return resourceBundle.getString("TimeConvert.label.About_?_Hours").replaceFirst("\\{0\\}", String.valueOf(value));
                        } else {
                            if (time < 172800) {
                                return resourceBundle.getString("TimeConvert.label.About_A_Day");
                            } else {
                                if (time < 2592000) {
                                    value = Math.round(time / 86400);
                                    return resourceBundle.getString("TimeConvert.label.About_?_Days").replaceFirst("\\{0\\}", String.valueOf(value));
                                } else {
                                    if (time < 5184000) {
                                        return resourceBundle.getString("TimeConvert.label.About_A_Month");
                                    } else {
                                        value = Math.round(time / 2592000);
                                        return resourceBundle.getString("TimeConvert.label.About_?_Months")
                                                .replaceFirst("\\{0\\}", String.valueOf(value));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
