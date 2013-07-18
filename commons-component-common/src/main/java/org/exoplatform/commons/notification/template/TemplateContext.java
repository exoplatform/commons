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
package org.exoplatform.commons.notification.template;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class TemplateContext extends HashMap<String, Object> {
  private static final long     serialVersionUID     = 1L;

  private static final Log       LOG                  = ExoLogger.getLogger(TemplateContext.class);

  private ConfigurationManager    configurationManager = null;

  private List<TemplateElement>   elementStacks        = new ArrayList<TemplateElement>();

  private StringWriter            writer;

  private static TemplateContext context              = null;
  
  private TemplateContext() {
    writer = new StringWriter();
  }
  
  public static TemplateContext getInstance() {
    if(context == null) {
      context = new TemplateContext();
      context.configurationManager = CommonsUtils.getService(ConfigurationManager.class);
    }
    context.clear();
    context.setWriter(new StringWriter());
    return context;
  }

  /**
   * @return the writer
   */
  public StringWriter getWriter() {
    return writer;
  }

  /**
   * @param writer the writer to set
   */
  public void setWriter(StringWriter writer) {
    this.writer = writer;
  }

  public void visit(TemplateElement element) {
    pushElement(element, true);
    try {
      element.accept(this).process();
    } finally {
      popElement();
    }
  }

  private void process() {
    TemplateElement element = elementStacks.iterator().next();
    GroovyTemplate gTemplate;
    try {
      TemplateContext context = element.getContext();
      if(element.getTemplateText() == null) {
        gTemplate = new GroovyTemplate(context.getTemplate(element.getResouceLocal()));
        element.setTemplateText(gTemplate.getText());
      } else {
        gTemplate = new GroovyTemplate(element.getTemplateText());
      }
      //
      gTemplate.render(context.getWriter(), context);
      element.setTemplate(context.getWriter().toString());
    } catch (Exception e) {
      LOG.warn("Can not process template " + element.getResouceLocal() + "\n" + e.getCause());
    }
  }
  
  public void popElement() {
    if(elementStacks.size() > 0) {
      elementStacks.remove(elementStacks.size() - 1);
    }
  }

  private void pushElement(TemplateElement element, boolean isClear) {
    elementStacks.add(element);
  }

  private InputStream getTemplateInputStream(String sourceLocal) throws Exception {
    try {
      String uri = sourceLocal;
      if (sourceLocal.indexOf("war") < 0 && sourceLocal.indexOf("jar") < 0) {
        URL url = null;
        if (sourceLocal.indexOf("/") == 0) {
          sourceLocal = sourceLocal.substring(1);
        }
        uri = "war:/" + sourceLocal;
        url = configurationManager.getURL(uri);
        if (url == null) {
          uri = "jar:/" + sourceLocal;
          url = configurationManager.getURL(uri);
        }
      }
      return configurationManager.getInputStream(uri);
    } catch (Exception e) {
      throw new RuntimeException("Error to get notification template " + sourceLocal, e);
    }
  }

  private Reader getTemplate(String sourceLocal) throws Exception {

    return new InputStreamReader(getTemplateInputStream(sourceLocal));
  }

}
