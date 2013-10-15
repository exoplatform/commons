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
package org.exoplatform.commons.notification.impl.service;

import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

@Managed
@ManagedDescription("Application statistic service")
@NameTemplate({ 
  @Property(key = "service", value = "sendEmailNotification")
})
public class SendEmailService implements ManagementAware {
  private boolean     isOn           = true;

  private long        emailSent      = 0;

  private int         emailPerMinute = 0;

  private ManagementContext context;
  
  private QueueMessageImpl queueMessage;

  public SendEmailService(QueueMessageImpl queueMessage) {
    this.queueMessage = queueMessage;
    this.queueMessage.setManagementView(this);
  }
  
  public void registerManager(Object o) {
    if (context != null) {
      context.register(o);
    }
  }

  @Override
  public void setContext(ManagementContext context) {
    this.context = context;
  }

  public void counter() {
    ++emailSent;
  }

  @Managed
  @ManagedDescription("Turn on the mail service.")
  @Impact(ImpactType.READ)
  public void on() {
    isOn = true;
    resetCounter();
  }

  @Managed
  @ManagedDescription("Status of mail service. (true/false)")
  @Impact(ImpactType.READ)
  public boolean isOn() {
    return isOn;
  }

  @Managed
  @ManagedDescription("Turn off the mail service.")
  @Impact(ImpactType.READ)
  public void off() {
    resetCounter();
    isOn = false;
  }

  @Managed
  @ManagedDescription("Number emails sent")
  @Impact(ImpactType.READ)
  public long sentCounter() {
    return emailSent;
  }

  @Managed
  @ManagedDescription("Reset email countet.")
  @Impact(ImpactType.READ)
  public void resetCounter() {
    emailSent = 0;
  }

  @Managed
  @ManagedDescription("Set number send emails per minute.")
  @Impact(ImpactType.WRITE)
  public void setNumberEmailPerMinute(int emailPerMinute) {
    this.emailPerMinute = emailPerMinute;
    this.queueMessage.runnable(timeSendingPerEmail());
  }

  /**
   * The millisecond to sending one email.
   * 
   * @return
   */
  private int timeSendingPerEmail() {
    if (emailPerMinute <= 0) {
      return 0;
    }
    return 60000 / emailPerMinute;
  }

}
