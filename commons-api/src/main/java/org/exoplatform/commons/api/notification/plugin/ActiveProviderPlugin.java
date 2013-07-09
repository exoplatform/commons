/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.plugin;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

public class ActiveProviderPlugin extends BaseComponentPlugin {
  private List<String> providerForAdmins   = new ArrayList<String>();

  private List<String> providerForAllUsers = new ArrayList<String>();
  
  public ActiveProviderPlugin(InitParams params) {
    ValuesParam vlsParamGlobal = params.getValuesParam("active.global");
    providerForAllUsers = new ArrayList<String>(vlsParamGlobal.getValues());
    ValuesParam vlsParamAdmin = params.getValuesParam("active.admin");
    providerForAdmins = new ArrayList<String>(vlsParamAdmin.getValues());

  }

  public List<String> getActiveProviderForUsers() {
    return removeEmptyItemList(providerForAllUsers);
  }

  public List<String> getActiveProviderForAdmins() {
    return removeEmptyItemList(providerForAdmins);
  }

  private List<String> removeEmptyItemList(List<String> list) {
    if (list == null)
      return new ArrayList<String>();

    while (list.contains("")) {
      list.remove("");
    }
    return list;
  }
}
