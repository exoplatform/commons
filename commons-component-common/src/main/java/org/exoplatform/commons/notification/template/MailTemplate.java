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
package org.exoplatform.commons.notification.template;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 1, 2013  
 */

public class MailTemplate extends SimpleElement {
  private String language;

  private final Element subject;

  private final Element digestSimple;

  private final Element digestOne;

  private final Element digestThree;

  private final Element digestMore;
  
  private TemplateContext context;
  
  public MailTemplate() {
    subject = new SimpleElement();
    digestSimple = new SimpleElement();
    digestOne = new SimpleElement();
    digestThree = new SimpleElement();
    digestMore = new SimpleElement();
  }
  /**
   * Sets the subject for Mail
   * @param template
   * @return
   */
  public MailTemplate subject(String template) {
    subject.template(template);
    return this;
  }
  
  /**
   * Sets the digestSimple
   * @param template
   * @return
   */
  public MailTemplate digestSimple(String template) {
    digestSimple.template(template);
    return this;
  }
  
  /**
   * Sets the digestOne
   * @param template
   * @return
   */
  public MailTemplate digestOne(String template) {
    digestOne.template(template);
    return this;
  }
  
  /**
   * Sets the digestThree
   * @param template
   * @return
   */
  public MailTemplate digestThree(String template) {
    digestThree.template(template);
    return this;
  }
  
  /**
   * Sets the digestThree
   * @param template
   * @return
   */
  public MailTemplate digestMore(String template) {
    digestMore.template(template);
    return this;
  }
  
  /**
   * 
   * @param context
   * @return
   */
  public MailTemplate with(TemplateContext context) {
    this.context = context;
    return this;
  }
  
  @Override
  public ElementVisitor accept(ElementVisitor visitor) {
    visitor.with(this.context);
    //TODO base on the type what we make the decision to visit
    
    return super.accept(visitor);
  }
  
}
