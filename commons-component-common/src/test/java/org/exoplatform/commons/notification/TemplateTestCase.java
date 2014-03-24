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
package org.exoplatform.commons.notification;

import junit.framework.TestCase;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;
import org.exoplatform.commons.notification.template.DigestTemplate;
import org.exoplatform.commons.notification.template.SimpleElement;
import org.exoplatform.commons.notification.template.SimpleElementVistior;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 2, 2013  
 */
public class TemplateTestCase extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  
  private Element makeSubject() {
    return new SimpleElement().template("$USER has joined $PORTAL_NAME");
  }
  
  private Element makeDigestOne() {
    return new SimpleElement().template("$USER has joined $PORTAL_NAME");
  }
  
  private Element makeDigestThree() {
    return new SimpleElement().template("$USER_LIST have joined $PORTAL_NAME.");
  }
  
  private Element makeDigestMore() {
    return new SimpleElement().template("$LAST3_USERS and $COUNT more have joined $PORTAL_NAME.");
  }
  
  private Element makeInstantly() {
    return new SimpleElement().template("$LAST3_USERS and $COUNT more have joined $PORTAL_NAME.");
  }
  
  private DigestTemplate makeDigestTemplate() {
    return new DigestTemplate().digestOne("$USER has joined $PORTAL_NAME")
                               .digestThree("$USER_LIST has joined $PORTAL_NAME")
                               .digestMore("$LAST3_USERS and $COUNT more have joined $PORTAL_NAME.");
  }
  
  public void testInstantlyMail() throws Exception {
    Element instantly = makeDigestMore().addNewLine(true);
    ElementVisitor visitor = SimpleElementVistior.instance();
    TemplateContext context = new TemplateContext();
    
    context.put("$LAST3_USERS", "root,mary,demo,jame");
    context.put("$COUNT", "4");
    context.put("$PORTAL_NAME", "intranet");
    String got = instantly.accept(visitor.with(context)).out();
    
    assertEquals("root,mary,demo,jame and 4 more have joined intranet.<br/>", got);
  }
  
  public void testDigestOne() throws Exception {
    Element instantly = makeDigestOne().addNewLine(true);
    ElementVisitor visitor = SimpleElementVistior.instance();
    TemplateContext context = new TemplateContext();
    
    context.put("$USER", "root");
    context.put("$PORTAL_NAME", "intranet");
    String got = instantly.accept(visitor.with(context)).out();
    
    assertEquals("root has joined intranet<br/>", got);
  }
  
  public void testDigestThree() throws Exception {
    Element instantly = makeDigestThree().addNewLine(true);
    ElementVisitor visitor = SimpleElementVistior.instance();
    TemplateContext context = new TemplateContext();
    
    context.put("$USER_LIST", "root,demo,mary");
    context.put("$PORTAL_NAME", "intranet");
    String got = instantly.accept(visitor.with(context)).out();
    
    assertEquals("root,demo,mary have joined intranet.<br/>", got);
  }
  
  public void testDigestMore() throws Exception {
    Element instantly = makeDigestThree().addNewLine(true);
    ElementVisitor visitor = SimpleElementVistior.instance();
    TemplateContext context = new TemplateContext();
    
    context.put("$USER_LIST", "root,demo,mary");
    context.put("$PORTAL_NAME", "intranet");
    String got = instantly.accept(visitor.with(context)).out();
    
    assertEquals("root,demo,mary have joined intranet.<br/>", got);
  }
  
  public void testDigest() throws Exception {
    DigestTemplate digest = makeDigestTemplate();
    ElementVisitor visitor = SimpleElementVistior.instance();
    TemplateContext context = new TemplateContext();
    
    context.put("$USER_LIST", "root,demo,mary");
    context.put("$PORTAL_NAME", "intranet");
    context.digestType(3);
    
    String got = digest.accept(visitor.with(context)).out();
    
    assertEquals("root,demo,mary has joined intranet", got);
  }
  

}
