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
package org.exoplatform.commons.notification;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.services.idgenerator.impl.IDGeneratorServiceImpl;
import org.exoplatform.services.jcr.util.IdGenerator;

public class NotificationUtilsTest extends TestCase {

  public NotificationUtilsTest() {
  }
  
  public void testGetLocale() {
    String language = null;
    Locale actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual);
    
    language = "";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual);
    
    language = "fr";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.FRENCH, actual);
    
    language = "pt_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR"), actual);
    
    language = "pt_BR_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR", "BR"), actual);
  }
  
  public void testIsValidEmailAddresses() {
    String emails = "";
    // email is empty
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    // email only text not @
    emails = "test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have @ but not '.'
    emails = "test@test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have charter strange
    emails = "#%^&test@test.com";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have before '.' is number
    emails = "test@test.787";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    
    emails = "no reply aaa@xyz.com, demo+aaa@demo.com, ";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    
    // basic case
    emails = "test@test.com";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com ,  demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test+test@test.com, demo+aaa@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
  }

  public void testProcessLinkInActivityTitle() throws Exception {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = NotificationUtils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
  }
  
  public void testCloneUserSettting() {
    UserSetting setting = UserSetting.getInstance();
    setting.setUserId("test");
    setting.setChannelActive("channel_test");
    //
    List<String> pluginIds = Arrays.asList("ActivityMentionPlugin,PostActivityPlugin,ActivityCommentPlugin,SpaceInvitationPlugin,RequestJoinSpacePlugin".split(","));
    for (String pluginId : pluginIds) {
      setting.addChannelPlugin("channel_test", pluginId);
    }
    UserSetting clone = setting.clone();
    //
    assertEquals(setting.getUserId(), clone.getUserId());
    //
    clone.setUserId("test1");
    clone.setChannelActive("channel_test1");
    clone.addChannelPlugin("channel_test", "NewUserPlugin");
    clone.addChannelPlugin("channel_test1", "NewUserPlugin");
    //
    assertFalse(setting.getUserId().equals(clone.getUserId()));
    //
    assertFalse(setting.getPlugins("channel_test").contains("NewUserPlugin"));
    assertTrue(clone.getPlugins("channel_test").contains("NewUserPlugin"));
    //
    assertTrue(setting.getPlugins("channel_test1").isEmpty());
    assertTrue(clone.getPlugins("channel_test1").contains("NewUserPlugin"));
    //
    assertTrue(setting.getChannelActives().contains("channel_test"));
    assertFalse(setting.getChannelActives().contains("channel_test1"));
    assertTrue(clone.getChannelActives().contains("channel_test"));
    assertTrue(clone.getChannelActives().contains("channel_test1"));
  }

  public void testNotificationInfoClone() {
    //
    new IdGenerator(new IDGeneratorServiceImpl());
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("test", "value test");
    NotificationInfo info = NotificationInfo.instance();
    info.setFrom("demo").key("notifiId").setOrder(1)
        .setOwnerParameter(ownerParameter)
        .setSendToDaily(new String[]{"plugin1", "plugin2"})
        .setSendToWeekly(new String[]{"plugin2", "plugin3"})
        .setTo("root");
    NotificationInfo clone = info.clone();
    assertEquals(info.getId(), clone.getId());
    assertEquals(info.getId(), clone.getId());
    assertEquals(info.getFrom(), clone.getFrom());
    assertEquals(info.getTo(), clone.getTo());
    //
    assertTrue(Arrays.equals(info.getSendToDaily(), clone.getSendToDaily()));
    assertTrue(Arrays.equals(info.getSendToWeekly(), clone.getSendToWeekly()));
    assertTrue(CollectionUtils.isEqualCollection(info.getOwnerParameter().keySet(), clone.getOwnerParameter().keySet()));
    assertTrue(CollectionUtils.isEqualCollection(info.getOwnerParameter().values(), clone.getOwnerParameter().values()));
    assertEquals(info.getValueOwnerParameter("test"), clone.getValueOwnerParameter("test"));
    //
    clone.getSendToDaily()[0] = "plugin4";
    clone.getOwnerParameter().put("test", "value clone");
    //
    assertFalse(Arrays.equals(info.getSendToDaily(), clone.getSendToDaily()));
    assertFalse(CollectionUtils.isEqualCollection(info.getOwnerParameter().values(), clone.getOwnerParameter().values()));
    //
    assertFalse(info.getValueOwnerParameter("test").equals(clone.getValueOwnerParameter("test")));
    //
    clone = info.clone(true);
    assertFalse(info.getId().equals(clone.getId()));
  }
}
