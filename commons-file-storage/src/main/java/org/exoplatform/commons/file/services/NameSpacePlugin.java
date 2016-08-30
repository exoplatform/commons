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
package org.exoplatform.commons.file.services;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class NameSpacePlugin extends BaseComponentPlugin {

  private Map<String, String> nameSpaceList = new HashMap<>();

  public NameSpacePlugin(InitParams initParams) {
    if (initParams != null) {
      PropertiesParam params = initParams.getPropertiesParam("fileNameSpace.params");
      if (params != null && !params.getProperty("name").isEmpty())
        nameSpaceList.put(params.getProperty("name"), params.getProperty("description"));
    }
  }

  public Map<String, String> getNameSpaceList() {
    return nameSpaceList;
  }

  public void setNameSpaceList(Map<String, String> nameSpaceList) {
    this.nameSpaceList = nameSpaceList;
  }

}
