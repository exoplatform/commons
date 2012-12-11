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
package org.exoplatform.commons.testing;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Nov 12, 2012  
 */
public abstract class BaseExoTestCase extends AbstractKernelTest {

  /** . */
  public static KernelBootstrap ownBootstrap = null;
  
  @Override
  public PortalContainer getContainer() {
     return ownBootstrap != null ? ownBootstrap.getContainer() : super.getContainer();
  }
  
  @Override
  protected void beforeRunBare() {
   if (ownBootstrap == null) {
     super.beforeRunBare();
   }
  }
  
  @Override
  protected void afterRunBare() {
    if (ownBootstrap == null && super.getContainer() != null) {
      super.afterRunBare();
    }
  }
  

}
