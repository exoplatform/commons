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
package org.exoplatform.webui.ckeditor;

import java.io.Writer;

import org.gatein.portal.controller.resource.ResourceScope;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * @Author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * May 10, 2011  
 */
public class UIFormCKEditorInput extends UIFormInputBase<String>{

  private CKEditorConfig editorConfig;
  
  public UIFormCKEditorInput(String name, String bindingField, String value) {
    super(name, bindingField, String.class);
    value_ = value;
  }
  
  
  
  /**
   * @return the editorConfig
   */
  public CKEditorConfig getEditorConfig() {
    return editorConfig;
  }



  /**
   * @param editorConfig the editorConfig to set
   */
  public void setEditorConfig(CKEditorConfig editorConfig) {
    this.editorConfig = editorConfig;
  }



  @Override
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    value_ = (String) input;
    if (value_ != null && value_.length() == 0)
      value_ = null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
   */
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer writer = context.getWriter();
    
    JavascriptManager jsManager = context.getJavascriptManager();
    //jsManager.loadScriptResource(ResourceScope.SHARED, "commons.editor");    
    RequireJS requirejs = jsManager.require("SHARED/commons.editor", "editor");         
      
    String ckBasePath = CKConfigListener.CK_CONTEXT_PATH;    
    requirejs.addScripts("window.CKEDITOR_BASEPATH='" + ckBasePath + "/ckeditor/';");    
    writer.write("<textarea id='" + name + "' name='" + name + "'>" + (value_ != null ? value_ : "") + "</textarea>");
    
    if (this.editorConfig != null && !this.editorConfig.isEmpty()) {
      String config = TagHelper.jsEncode(this.editorConfig);
      requirejs.addScripts("editor.EXOCKEDITOR.makeCKEditor('" + name + "', " + config + ");");
    } else {
      requirejs.addScripts("editor.EXOCKEDITOR.makeCKEditor('" + name + "');");    
    }
  }

  
  
}
