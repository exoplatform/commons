/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.template;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 8, 2013  
 */
public class GroovyElement extends SimpleElement {

  public String appRes(String key) {
    Locale locale = Locale.ENGLISH;
    if (getLanguage() != null && getLanguage().length() > 0) {
      locale = new Locale(getLanguage());
    }
    return TemplateUtils.getResourceBundle(key, locale, getTemplateConfig().getBundlePath());
  }

  public String appRes(String key, String... strs) {
    
    String value = appRes(key);
    if (strs != null && strs.length > 0) {
      for (int i = 0; i < strs.length; ++i) {
        value = StringUtils.replace(value, "{" + i + "}", strs[i]);
      }
    }
    return value;
  }
}
