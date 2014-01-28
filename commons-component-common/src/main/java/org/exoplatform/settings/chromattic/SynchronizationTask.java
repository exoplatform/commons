  /*
   * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.settings.chromattic;
  
  import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
  import org.exoplatform.commons.chromattic.SessionContext;
  import org.exoplatform.container.PortalContainer;
  import org.exoplatform.settings.impl.SettingServiceImpl;
  
  /**
   * Created by The eXo Platform SAS
   * Author : thanh_vucong
   *          thanh_vucong@exoplatform.com
   * Dec 10, 2012  
   */
  public abstract class SynchronizationTask<V>
  {
  
     /**
      * Executes a task within a context from the specified life cycle. If an existing context already exists
      * then this context is used otherwise a context is managed for the duration of the {@link #execute(SessionContext)}
      * method.
      *
      * @param lifeCycle the life cycle
      * @return a value
      */
    public final V executeWith(ChromatticLifeCycle lifeCycle) {
        PortalContainer container = PortalContainer.getInstance();
        SettingServiceImpl settingServiceImpl = (SettingServiceImpl) container.getComponentInstanceOfType(SettingServiceImpl.class);
      boolean created = settingServiceImpl.startSynchronization();
      try {
        return execute(lifeCycle.getContext());
      }
      finally {
          settingServiceImpl.stopSynchronization(created);
      }
    }
  
     /**
      * Implementor must provide the task logic here.
      *
      * @param context the context
      * @return a value
      */
     protected abstract V execute(SessionContext ctx);
     
     
  }