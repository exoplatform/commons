/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.channel.template;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public abstract class TemplateProvider extends BaseComponentPlugin {
  private Map<String, String> templateFilePaths = new HashMap<String, String>();

  public TemplateProvider() {
    // parser the annotation and build the template map
    TemplateConfigs templates = this.getClass().getAnnotation(TemplateConfigs.class);
    if (templates != null) {
      for (TemplateConfig config : templates.templates()) {
        if (config != null && config.pluginId() != "") {
          templateFilePaths.put(config.pluginId(), config.template());
        }
      }
    }

  }
  /**
   * Gets all of the template files
   * @return
   */
  public Map<String, String> getTemplateConfigs() {
    return templateFilePaths;
  }

}