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

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.TemplateTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 1, 2013  
 */
public class SimpleTemplateTransformer implements TemplateTransformer {
  
  private String template;

  @Override
  public TemplateTransformer from(String template) {
    this.template = template;
    return this;
  }

  @Override
  public String transform(TemplateContext context) {
    String got = template;
    String newKey = "";
    //
    for (String key : context.keySet()) {
      newKey = key.indexOf("$") == 0 ? key : "$" + key;
      got = got.replace(newKey, (String) context.get(key));
    }
    return got;
  }

}
