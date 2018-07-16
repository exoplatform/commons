package org.exoplatform.commons.api.notification.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;


public class WebNotificationFilter {
  
  private PluginKey pluginKey;
  private final String userId;
  private int limitDay = 0;
  private boolean onPopover = false;
  private Boolean isRead = null;
  private boolean isOrder = true;
  private Pair<String, String> parameter;

  public WebNotificationFilter(String userId) {
    this.userId = userId;
  }

  public WebNotificationFilter(String userId, boolean onPopover) {
    this.userId = userId;
    this.onPopover = onPopover;
  }

  public PluginKey getPluginKey() {
    return pluginKey;
  }

  public WebNotificationFilter setPluginKey(PluginKey pluginKey) {
    this.pluginKey = pluginKey;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public int getLimitDay() {
    return limitDay;
  }

  public void setLimitDay(int limitDay) {
    this.limitDay = limitDay;
  }

  public boolean isOnPopover() {
    return onPopover;
  }

  public WebNotificationFilter setOnPopover(boolean onPopover) {
    this.onPopover = onPopover;
    return this;
  }

  public Boolean isRead() {
    return isRead;
  }

  public WebNotificationFilter setRead(boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  public boolean isOrder() {
    return isOrder;
  }

  public WebNotificationFilter setOrder(boolean isOrder) {
    this.isOrder = isOrder;
    return this;
  }

  public Pair<String, String> getParameter() {
    return parameter;
  }

  public WebNotificationFilter setParameter(String paramName, String paramValue) {
    this.parameter = new ImmutablePair<>(paramName, paramValue);
    return this;
  }
  
  @Override
  public String toString() {
    try {
      return new JSONObject(this).toString();
    } catch (Exception e) {
      return "[WebNotificationFilter: userId = " + userId + " ]";
    }
  }

}
