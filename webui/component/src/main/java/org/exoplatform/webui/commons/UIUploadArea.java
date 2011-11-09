/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.webui.commons;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Nov 3, 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "classpath:groovy/webui/commons/UIUploadArea.gtmpl",
  events = {
    @EventConfig(listeners = UIUploadArea.RefreshUploadActionListener.class)
  }   
)
public class UIUploadArea extends UIContainer {
  
  public static final String UPLOAD_INPUT          = "UIUploadInput";
  
  public static int            SIZE_LIMIT            = -1;
  
  protected static final String REFRESH_UPLOAD = "RefreshUpload";

  public UIUploadArea() {
    UIUploadInput uiInput = new UIUploadInput(UPLOAD_INPUT, UPLOAD_INPUT, SIZE_LIMIT);
    uiInput.setAutoUpload(true);
    addChild(uiInput);
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.getChild(UIUploadInput.class).setRefreshUploadAction(this.event(REFRESH_UPLOAD));
    super.processRender(context);
  }

  static public class RefreshUploadActionListener extends EventListener<UIUploadArea> {
    public void execute(Event<UIUploadArea> event) throws Exception {
      UIUploadArea component = event.getSource();
      component.removeChild(UIUploadInput.class);
      UIUploadInput uiInput = new UIUploadInput(UIUploadArea.UPLOAD_INPUT, UIUploadArea.UPLOAD_INPUT, UIUploadArea.SIZE_LIMIT);
      uiInput.setAutoUpload(true);
      component.addChild(uiInput);
      event.getRequestContext().addUIComponentToUpdateByAjax(component);
    }
  }
}
