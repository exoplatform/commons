/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.commons.utils;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.deployment.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Dec 24, 2012  
 */
public class TestActivityTypeUtils extends BaseCommonsTestCase {
  private static final String ID_TEST          = "testsocialcommons";

  private static final String FAKE_ACTIVITY_DI = "activityIdfake";

  public TestActivityTypeUtils() {
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (root.hasNode(ID_TEST)) {
      root.getNode(ID_TEST).remove();
      session.save();
    }
  }

  private void saveNode(Node node) throws Exception {
    if (root.isNew() || node.isNew()) {
      session.save();
    } else {
      node.save();
    }
  }

  public void testAttachActivityId() throws Exception {
    Node testNode = Utils.makePath(root, ID_TEST, "nt:unstructured");
    assertNotNull(testNode);
    ActivityTypeUtils.attachActivityId(testNode, FAKE_ACTIVITY_DI);
    saveNode(testNode);
    assertEquals(true, testNode.isNodeType(ActivityTypeUtils.EXO_ACTIVITY_INFO));
    assertEquals(FAKE_ACTIVITY_DI, testNode.getProperty(ActivityTypeUtils.EXO_ACTIVITY_ID).getString());
  }

  public void testGetActivityId() throws Exception {
    Node testNode = Utils.makePath(root, ID_TEST, "nt:unstructured");
    assertNotNull(testNode);

    // not save
    assertNull(ActivityTypeUtils.getActivityId(testNode));

    // save
    ActivityTypeUtils.attachActivityId(testNode, FAKE_ACTIVITY_DI);
    saveNode(testNode);

    //
    assertEquals(FAKE_ACTIVITY_DI, ActivityTypeUtils.getActivityId(testNode));
  }

  public void testRemoveAttchAtivityId() throws Exception {
    Node testNode = Utils.makePath(root, ID_TEST, "nt:unstructured");
    assertNotNull(testNode);

    // not save
    ActivityTypeUtils.removeAttchAtivityId(testNode);
    saveNode(testNode);
    assertEquals(true, testNode.isNodeType(ActivityTypeUtils.EXO_ACTIVITY_INFO));
    assertEquals(StringUtils.EMPTY, ActivityTypeUtils.getActivityId(testNode));

    // save
    ActivityTypeUtils.attachActivityId(testNode, FAKE_ACTIVITY_DI);
    saveNode(testNode);

    //
    ActivityTypeUtils.removeAttchAtivityId(testNode);
    saveNode(testNode);
    assertEquals(StringUtils.EMPTY, ActivityTypeUtils.getActivityId(testNode));
  }

}