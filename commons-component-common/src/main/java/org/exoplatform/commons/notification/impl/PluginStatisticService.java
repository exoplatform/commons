/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.notification.impl;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 23, 2013  
 */
@SuppressWarnings("unused")
@Managed
@NameTemplate({@Property(key = "service", value = "notification"), @Property(key = "view", value = "PluginNotifStatistic") })
@ManagedDescription("Notifiaction plugin statistics service.")
public class PluginStatisticService {
  private long activityCommentCreatedMessageCount = 0;
  private long activityCommentCreatedNotifCount = 0;
  private long activityCommentCreatedDigestCount = 0;
  
  private long activityMentionCreatedMessageCount = 0;
  private long activityMentionCreatedNotifCount = 0;
  private long activityMentionCreatedDigestCount = 0;
  
  private long likeCreatedMessageCount = 0;
  private long likeCreatedNotifCount = 0;
  private long likeCreatedDigestCount = 0;
  
  private long newUserCreatedMessageCount = 0;
  private long newUserCreatedNotifCount = 0;
  private long newUserCreatedDigestCount = 0;
  
  private long postActivityCreatedMessageCount = 0;
  private long postActivityCreatedNotifCount = 0;
  private long postActivityCreatedDigestCount = 0;
  
  private long postActivitySpaceCreatedMessageCount = 0;
  private long postActivitySpaceCreatedNotifCount = 0;
  private long postActivitySpaceCreatedDigestCount = 0;
  
  private long relationshipRecievedCreatedMessageCount = 0;
  private long relationshipRecievedCreatedNotifCount = 0;
  private long relationshipRecievedCreatedDigestCount = 0;
  
  private long requestJoinSpaceCreatedMessageCount = 0;
  private long requestJoinSpaceCreatedNotifCount = 0;
  private long requestJoinSpaceCreatedDigestCount = 0;
  
  private long spaceInvitationCreatedMessageCount = 0;
  private long spaceInvitationCreatedNotifCount = 0;
  private long spaceInvitationCreatedDigestCount = 0;
  
  /**
   * Increase value when plugin creates new message
   * 
   * @param pluginId
   */
  public void increaseCreatedMessageCount(String pluginId) {
    if ("ActivityCommentPlugin".equalsIgnoreCase(pluginId)) {
      this.activityCommentCreatedMessageCount++;
    } else if ("ActivityMentionPlugin".equalsIgnoreCase(pluginId)) {
      activityMentionCreatedMessageCount++;
    } else if ("LikePlugin".equalsIgnoreCase(pluginId)) {
      likeCreatedMessageCount++;
    } else if ("NewUserPlugin".equalsIgnoreCase(pluginId)) {
      newUserCreatedMessageCount++;
    } else if ("PostActivityPlugin".equalsIgnoreCase(pluginId)) {
      postActivityCreatedMessageCount++;
    } else if ("PostActivitySpaceStreamPlugin".equalsIgnoreCase(pluginId)) {
      postActivitySpaceCreatedMessageCount++;
    } else if ("RelationshipRecievedRequestPlugin".equalsIgnoreCase(pluginId)) {
      relationshipRecievedCreatedMessageCount++;
    } else if ("RequestJoinSpacePlugin".equalsIgnoreCase(pluginId)) {
      requestJoinSpaceCreatedMessageCount++;
    } else if ("SpaceInvitationPlugin".equalsIgnoreCase(pluginId)) {
      spaceInvitationCreatedMessageCount++;
    }
    
  }
  
  /**
   * Increase value when plugin creates new notification message
   * @param pluginId
   */
  public void increaseCreatedNotifCount(String pluginId) {
    if ("ActivityCommentPlugin".equalsIgnoreCase(pluginId)) {
      activityCommentCreatedNotifCount++;
    } else if ("ActivityMentionPlugin".equalsIgnoreCase(pluginId)) {
      activityMentionCreatedNotifCount++;
    } else if ("LikePlugin".equalsIgnoreCase(pluginId)) {
      likeCreatedNotifCount++;
    } else if ("NewUserPlugin".equalsIgnoreCase(pluginId)) {
      newUserCreatedNotifCount++;
    } else if ("PostActivityPlugin".equalsIgnoreCase(pluginId)) {
      postActivityCreatedNotifCount++;
    } else if ("PostActivitySpaceStreamPlugin".equalsIgnoreCase(pluginId)) {
      postActivitySpaceCreatedNotifCount++;
    } else if ("RelationshipRecievedRequestPlugin".equalsIgnoreCase(pluginId)) {
      relationshipRecievedCreatedNotifCount++;
    } else if ("RequestJoinSpacePlugin".equalsIgnoreCase(pluginId)) {
      requestJoinSpaceCreatedNotifCount++;
    } else if ("SpaceInvitationPlugin".equalsIgnoreCase(pluginId)) {
      spaceInvitationCreatedNotifCount++;
    }
  }
  
  /**
   * Increase value when plugin creates new digest message
   * 
   * @param pluginId
   */
  public void increaseCreatedDigestCount(String pluginId) {
    if ("ActivityCommentPlugin".equalsIgnoreCase(pluginId)) {
      this.activityCommentCreatedDigestCount++;
    } else if ("ActivityMentionPlugin".equalsIgnoreCase(pluginId)) {
      this.activityMentionCreatedDigestCount++;
    } else if ("LikePlugin".equalsIgnoreCase(pluginId)) {
      this.likeCreatedDigestCount++;
    } else if ("NewUserPlugin".equalsIgnoreCase(pluginId)) {
      this.newUserCreatedDigestCount++;
    } else if ("PostActivityPlugin".equalsIgnoreCase(pluginId)) {
      this.postActivityCreatedDigestCount++;
    } else if ("PostActivitySpaceStreamPlugin".equalsIgnoreCase(pluginId)) {
      this.postActivitySpaceCreatedDigestCount++;
    } else if ("RelationshipRecievedRequestPlugin".equalsIgnoreCase(pluginId)) {
      this.relationshipRecievedCreatedDigestCount++;
    } else if ("RequestJoinSpacePlugin".equalsIgnoreCase(pluginId)) {
      this.requestJoinSpaceCreatedDigestCount++;
    } else if ("SpaceInvitationPlugin".equalsIgnoreCase(pluginId)) {
      this.spaceInvitationCreatedDigestCount++;
    }
  }

  @Managed
  public long getActivityCommentCreatedMessageCount() {
    return activityCommentCreatedMessageCount;
  }

  @Managed
  public long getActivityCommentCreatedNotifCount() {
    return activityCommentCreatedNotifCount;
  }

  @Managed
  public long getActivityCommentCreatedDigestCount() {
    return activityCommentCreatedDigestCount;
  }

  @Managed
  public long getActivityMentionCreatedMessageCount() {
    return activityMentionCreatedMessageCount;
  }

  @Managed
  public long getActivityMentionCreatedNotifCount() {
    return activityMentionCreatedNotifCount;
  }

  @Managed
  public long getActivityMentionCreatedDigestCount() {
    return activityMentionCreatedDigestCount;
  }

  @Managed
  public long getLikeCreatedMessageCount() {
    return likeCreatedMessageCount;
  }

  @Managed
  public long getLikeCreatedNotifCount() {
    return likeCreatedNotifCount;
  }

  @Managed
  public long getLikeCreatedDigestCount() {
    return likeCreatedDigestCount;
  }

  @Managed
  public long getNewUserCreatedMessageCount() {
    return newUserCreatedMessageCount;
  }

  @Managed
  public long getNewUserCreatedNotifCount() {
    return newUserCreatedNotifCount;
  }

  @Managed
  public long getNewUserCreatedDigestCount() {
    return newUserCreatedDigestCount;
  }

  @Managed
  public long getPostActivityCreatedMessageCount() {
    return postActivityCreatedMessageCount;
  }

  @Managed
  public long getPostActivityCreatedNotifCount() {
    return postActivityCreatedNotifCount;
  }

  @Managed
  public long getPostActivityCreatedDigestCount() {
    return postActivityCreatedDigestCount;
  }

  @Managed
  public long getPostActivitySpaceCreatedMessageCount() {
    return postActivitySpaceCreatedMessageCount;
  }

  @Managed
  public long getPostActivitySpaceCreatedNotifCount() {
    return postActivitySpaceCreatedNotifCount;
  }

  @Managed
  public long getPostActivitySpaceCreatedDigestCount() {
    return postActivitySpaceCreatedDigestCount;
  }

  @Managed
  public long getRelationshipRecievedCreatedMessageCount() {
    return relationshipRecievedCreatedMessageCount;
  }

  @Managed
  public long getRelationshipRecievedCreatedNotifCount() {
    return relationshipRecievedCreatedNotifCount;
  }

  @Managed
  public long getRelationshipRecievedCreatedDigestCount() {
    return relationshipRecievedCreatedDigestCount;
  }

  @Managed
  public long getRequestJoinSpaceCreatedMessageCount() {
    return requestJoinSpaceCreatedMessageCount;
  }

  @Managed
  public long getRequestJoinSpaceCreatedNotifCount() {
    return requestJoinSpaceCreatedNotifCount;
  }

  @Managed
  public long getRequestJoinSpaceCreatedDigestCount() {
    return requestJoinSpaceCreatedDigestCount;
  }

  @Managed
  public long getSpaceInvitationCreatedMessageCount() {
    return spaceInvitationCreatedMessageCount;
  }

  @Managed
  public long getSpaceInvitationCreatedNotifCount() {
    return spaceInvitationCreatedNotifCount;
  }

  @Managed
  public long getSpaceInvitationCreatedDigestCount() {
    return spaceInvitationCreatedDigestCount;
  }
  
  
}
