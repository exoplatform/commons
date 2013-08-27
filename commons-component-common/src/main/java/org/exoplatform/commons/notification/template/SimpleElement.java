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

import java.util.Locale;

import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 1, 2013  
 */
public class SimpleElement implements Element {

  /**
   * The language
   */
  private String language;
  /**
   * The template
   */
  private String template;
  
  /**
   * The template configure
   */
  private TemplateConfig templateConfig;
  
  /**
   * need add new line tag
   */
  private boolean newLine = false;
  
  public SimpleElement() {
    this.language = Locale.ENGLISH.getLanguage();
  }
  
  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public ElementVisitor accept(ElementVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public String getTemplate() {
    return this.template;
  }

  @Override
  public Element language(String language) {
    this.language = language;
    return this;
  }

  @Override
  public Element template(String template) {
    this.template = template;
    return this;
  }
  
  @Override
  public Element config(TemplateConfig templateConfig) {
    this.templateConfig = templateConfig;
    return this;
  }
  
  
  @Override
  public String toString() {
    return "[language = " + this.language +"; template = " + this.template + "]";
  }

  @Override
  public TemplateConfig getTemplateConfig() {
    return this.templateConfig;
  }

  @Override
  public Element addNewLine(boolean needNewLine) {
    this.newLine = needNewLine;
    return this;
  }

  @Override
  public boolean isNewLine() {
    return this.newLine;
  }
}
