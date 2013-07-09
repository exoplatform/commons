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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

public class GroupProviderPlugin extends BaseComponentPlugin {
  
  private List<GroupProviderModel> groupProviders = new ArrayList<GroupProviderModel>();

  public GroupProviderPlugin(InitParams params) {
    //
    groupProviders = params.getObjectParamValues(GroupProviderModel.class);
  }

  public List<GroupProviderModel> getGroupProviders() {
    return groupProviders;
  }
  
  public static void addGroupProviderData(List<GroupProviderModel> srcgroupProviders, List<GroupProviderModel> destGroupProviders) {
    if (destGroupProviders.size() > 0) {
      //
      for (GroupProviderModel key : destGroupProviders) {
        if (srcgroupProviders.contains(key) == false) {
          srcgroupProviders.add(key);
        }
      }
      //
      Collections.sort(srcgroupProviders, new ComparatorGroupProviderASC());
    }
  }
  
  private static class ComparatorGroupProviderASC implements Comparator<GroupProviderModel> {
    public int compare(GroupProviderModel o1, GroupProviderModel o2) throws ClassCastException {
      String name1 = o1.getName();
      String name2 = o2.getName();
      return name1.compareTo(name2);
    }
  }
}
