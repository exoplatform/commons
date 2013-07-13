package org.exoplatform.services.bench;
/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */


import java.util.HashMap;

import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 2, 2011  
 */
public class FakeDataInjector extends DataInjector {
  
  private boolean isInjected = false;
  
  
  
  @Override
  public String getName() {
    return FakeDataInjector.class.getName();
  }

  @Override
  public Log getLog() {
    return null;
  }

  public boolean isInitialized() {
    return isInjected;
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    // TODO Auto-generated method stub
    return new Object();
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    isInjected = true;
  }

  @Override
  public void reject(HashMap<String, String> params) throws Exception {
    isInjected = false;
  }
  
  
  
}
