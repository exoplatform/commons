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
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;

public class DigestorProviderImpl extends AbstractNotificationProvider implements NotificationProviderService {

  private List<AbstractNotificationProvider> listSupportProviderImpl = new ArrayList<AbstractNotificationProvider>();

  public DigestorProviderImpl() {
  }
  
  @Override
  public void addSupportProviderImpl(AbstractNotificationProvider providerImpl) {
    listSupportProviderImpl.add(providerImpl);
  }
  
  @Override
  public AbstractNotificationProvider getSupportProviderImpl(String providerType) {
    for (AbstractNotificationProvider providerImpl : listSupportProviderImpl) {
      if(providerImpl.getSupportType().contains(providerType)) {
        return providerImpl;
      }
    }
    return null;
  }

  @Override
  public MessageInfo buildMessageInfo(NotificationMessage message) {
    return null;
  }

  @Override
  public List<String> getSupportType() {
    return new ArrayList<String>();
  }


}
