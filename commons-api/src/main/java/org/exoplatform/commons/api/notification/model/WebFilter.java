package org.exoplatform.commons.api.notification.model;

import org.json.JSONObject;


public class WebFilter {
  private PluginKey pluginKey;
  private String    userId;
  private String    jcrPath;
  private int offset = 0;
  private int limit = 0;
  private int limitDay = 0;
  private boolean onPopover = false;
  private Boolean isRead = null;
  private boolean isOrder = true;

  public WebFilter(String userId, int offset, int limit) {
    this.userId = userId;
    this.offset = offset;
    this.limit = limit;
  }

  public WebFilter(String userId, boolean onPopover, int limit) {
    this.userId = userId;
    this.onPopover = onPopover;
    this.limit = limit;
  }

  public PluginKey getPluginKey() {
    return pluginKey;
  }

  public WebFilter setPluginKey(PluginKey pluginKey) {
    this.pluginKey = pluginKey;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public WebFilter setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public int getOffset() {
    return offset;
  }

  public WebFilter setOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public WebFilter setLimit(int limit) {
    this.limit = limit;
    return this;
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

  public WebFilter setOnPopover(boolean onPopover) {
    this.onPopover = onPopover;
    return this;
  }

  public Boolean isRead() {
    return isRead;
  }

  public WebFilter setRead(boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  public boolean isOrder() {
    return isOrder;
  }

  public WebFilter setOrder(boolean isOrder) {
    this.isOrder = isOrder;
    return this;
  }
  
  public String getJcrPath() {
    return jcrPath;
  }

  public void setJcrPath(String jcrPath) {
    this.jcrPath = jcrPath;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof WebFilter) {
      WebFilter that = (WebFilter) o;
      if (pluginKey != null ? !pluginKey.equals(that.pluginKey) : that.pluginKey != null) {
        return false;
      }
      if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
        return false;
      }
      if (isRead != null ? !isRead.equals(that.isRead) : that.isRead != null) {
        return false;
      }
      if (onPopover != that.onPopover || offset != that.offset ||
          limit != that.limit || isOrder != that.isOrder) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    final int PRIME = 31;
    result = PRIME * result + ((pluginKey != null) ? pluginKey.hashCode() : 0);
    result = PRIME * result + ((userId != null) ? userId.hashCode() : 0);
    result = PRIME * result + ((isRead != null && isRead.booleanValue()) ? 1 : 0);
    result = PRIME * result + ((isOrder) ? 1 : 0);
    result = PRIME * result + ((onPopover) ? 1 : 0);
    result = PRIME * result + offset;
    result = PRIME * result + limit;
    return result;
  }

  @Override
  public String toString() {
    try {
      return new JSONObject(this).toString();
    } catch (Exception e) {
      return "[WebFilter: userId = " + userId + " ]";
    }
  }

}
