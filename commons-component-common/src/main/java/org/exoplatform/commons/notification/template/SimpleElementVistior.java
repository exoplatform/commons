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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;
import org.exoplatform.commons.api.notification.template.TemplateTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 1, 2013  
 */
public class SimpleElementVistior implements ElementVisitor {
  private final static String BREAK_LINE = "<br/>";
  private final TemplateTransformer transformer;
  private final Writer writer;
  private TemplateContext ctx;
  
  /**
   * Create new instance of ElementVisitor
   * @return
   */
  public static ElementVisitor instance() {
    return new SimpleElementVistior();
  }
  
  private SimpleElementVistior() {
    transformer = new SimpleTemplateTransformer();
    writer = new StringWriter();
  }

  @Override
  public ElementVisitor visit(Element element) {
    String value = transformer.from(element.getTemplate()).transform(this.ctx);
    try {
      writer.append(value);
      if (element.isNewLine()) {
        writer.append(BREAK_LINE);
      }
    } catch (IOException e) {
      ctx.setException(e);
    }
    return this;
  }

  @Override
  public String out() {
    return writer.toString();
  }

  @Override
  public ElementVisitor with(TemplateContext ctx) {
    this.ctx = ctx;
    return this;
  }
  
  @Override
  public String toString() {
    return writer.toString();
  }

  @Override
  public TemplateContext getTemplateContext() {
    return this.ctx;
  }

  @Override
  public Writer getWriter() {
    return this.writer;
  }

}
