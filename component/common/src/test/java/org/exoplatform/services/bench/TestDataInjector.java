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
package org.exoplatform.services.bench;


import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 2, 2011  
 */
public class TestDataInjector extends TestCase {
  
  FakeDataInjector dataInjector;
  
  HashMap<String, String> queryParams;
  
  @Override
  protected void setUp() throws Exception {
    dataInjector = new FakeDataInjector();
    queryParams = new HashMap<String, String>();
    queryParams.put("groups", "validator:/platform/administrators,manager:/platform/users");
    queryParams.put("users", "root,john,marry,james,demo");
    queryParams.put("memship", "manager,member,validator");
    super.setUp();
  }

  public void testRandomUser() {
    assertNotNull(dataInjector.randomUser());
  }
  
  public void testRandomWords() {
    assertNotNull(dataInjector.randomWords(10));
  }
  
  public void testRandomParagraph() {
    assertNotNull(dataInjector.randomParagraphs(10));
  }
  
  public void testCreateTextResource() {
    assertTrue(dataInjector.createTextResource(10).getBytes().length == (10 * 1024));
  }
  
  /**
   * Test function: DataInjector.readGroupsIfExist()
   */
  public void testReadGroupsIfExist() {
    List<String> groups = dataInjector.readGroupsIfExist(queryParams);
    assertNotNull(groups);
    assertEquals(2, groups.size());
    assertEquals("validator:/platform/administrators", groups.get(0));
    assertEquals("manager:/platform/users", groups.get(1));
  }
  
  /**
   * Test function: DataInjector.readUsersIfExist()
   */
  public void testReadUsersIfExist() {
    List<String> users = dataInjector.readUsersIfExist(queryParams);
    assertNotNull(users);
    assertEquals(5, users.size());
    assertEquals("root", users.get(0));
    assertEquals("john", users.get(1));
    assertEquals("marry", users.get(2));
    assertEquals("james", users.get(3));
    assertEquals("demo", users.get(4));    
  }
  
  /**
   * Test function: DataInjector.readMembershipIfExist()
   */
  public void testReadMembershipIfExist() {
    List<String> memberships = dataInjector.readMembershipIfExist(queryParams);
    assertNotNull(memberships);
    assertEquals(3, memberships.size());
    assertEquals("manager", memberships.get(0));
    assertEquals("member", memberships.get(1));
    assertEquals("validator", memberships.get(2));
  }
  
  protected void tearDown() throws Exception {
    dataInjector = null;
    queryParams = null;
  }  
}

