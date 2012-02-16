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


import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 2, 2011  
 */
public class TestDataInjector extends TestCase {
  
  FakeDataInjector dataInjector;
  
  @Override
  protected void setUp() throws Exception {
    dataInjector = new FakeDataInjector();
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
  
}
