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
@ManagedDescription("Mock mail service")
@NameTemplate({ 
  @Property(key = "service", value = "notification"), 
  @Property(key = "view", value = "mockmail")
})
public class SendEmailService implements ManagementAware {
  private boolean     isOn           = false;

  private long        sentCounter     = 0;

  private long        currentCapacity = 0;

  private int         emailPerSend  = 0;

  private int         interval  = 0;
  
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
    ++sentCounter;
  }

  public void addCurrentCapacity() {
    ++currentCapacity;
  }

  public void removeCurrentCapacity() {
    if (currentCapacity > 0) {
      --currentCapacity;
    }
  }

  @Managed
  @ManagedDescription("Current mail service capacity should be available.")
  @Impact(ImpactType.READ)
  public long getCurrentCapacity() {
    return currentCapacity;
  }
  
  @Managed
  @ManagedDescription("Turn on the mail service.")
  @Impact(ImpactType.READ)
  public void on() {
    resetCounter();
    isOn = true;
    emailPerSend = 120;
    interval = 120;
    makeJob();
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
    this.queueMessage.resetDefaultConfigJob();
    isOn = false;
  }

  @Managed
  @ManagedDescription("Number emails sent")
  @Impact(ImpactType.READ)
  public long getSentCounter() {
    return sentCounter;
  }

  @Managed
  @ManagedDescription("Reset email countet.")
  @Impact(ImpactType.READ)
  public void resetCounter() {
    sentCounter = 0;
  }

  @Managed
  @ManagedDescription("Set number emails send per one time.")
  @Impact(ImpactType.READ)
  public void setNumberEmailPerSend(int emailPerSend) {
    this.emailPerSend = emailPerSend;
    makeJob();
  }

  @Managed
  @ManagedDescription("Number emails send per one time.")
  @Impact(ImpactType.READ)
  public int getNumberEmailPerSend() {
    return this.emailPerSend;
  }

  @Managed
  @ManagedDescription("Set period of time (in seconds) for each sending notification execution.")
  @Impact(ImpactType.READ)
  public void setInterval(int interval) {
    this.interval = interval;
    makeJob();
  }
  
  @Managed
  @ManagedDescription("Period of time (in seconds) between each sending notification execution.")
  @Impact(ImpactType.READ)
  public int getInterval() {
    return this.interval;
  }

  @Managed
  @ManagedDescription("Removes all notification data that stored in database.")
  @Impact(ImpactType.READ)
  public String resetTestMail() {
    currentCapacity = 0;
    resetCounter();
    isOn = true;
    emailPerSend = 120;
    interval = 120;
    return queueMessage.removeAll();
  }
  
  private void makeJob() {
    if (isOn) {
      this.queueMessage.makeJob(emailPerSend, interval * 1000);
    }
  }
}
