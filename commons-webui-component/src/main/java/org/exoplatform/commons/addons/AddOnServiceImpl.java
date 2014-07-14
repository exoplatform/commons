/**
 * Copyright (C) 2014 eXo Platform SAS.
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

package org.exoplatform.commons.addons;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.model.Application;

public class AddOnServiceImpl implements AddOnService {

    private Map<String, List<AddOnPlugin>> plugins = new HashMap<String, List<AddOnPlugin>>();

    @Override
    public List<Application<?>> getApplications(String containerName) {
        List<Application<?>> apps = new LinkedList<Application<?>>();

        List<AddOnPlugin> ls = plugins.get(containerName);
        if (ls != null) {
            for (AddOnPlugin p : ls) {
                apps.addAll(p.getApplications());
            }
        }
        return apps;
    }

    @Override
    public void addPlugin(AddOnPlugin plugin) {
        List<AddOnPlugin> ls = plugins.get(plugin.getContainerName());
        if (ls == null) {
            ls = new LinkedList<AddOnPlugin>();
            plugins.put(plugin.getContainerName(), ls);
        }
        ls.add(plugin);
        Collections.sort(ls, new Comparator<AddOnPlugin>() {
            @Override
            public int compare(AddOnPlugin o1, AddOnPlugin o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
    }
}