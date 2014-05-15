/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.services.user;


public class UserStateModel {
  
  private String userId = null;
      
  private long lastActivity = 0;
  
  private String status = null;
  
  public UserStateModel() {}
  
  public UserStateModel(String userId, long lastActivity, String status) {
    this.userId = userId;
    this.lastActivity = lastActivity;
    this.status = status;
  }
  
  public String getUserId() {
    return this.userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public long getLastActivity() {
    return this.lastActivity;
  }
  
  public void setLastActivity(long lastActivity) {
    this.lastActivity = lastActivity;
  }
  
  public String getStatus() {
    return this.status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public UserStateModel clone() {
    return new UserStateModel(this.getUserId(), this.getLastActivity(), this.getStatus());
  }
}
