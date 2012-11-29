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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Nov 2, 2011  
 */
@ComponentConfig(
  template = "classpath:groovy/webui/commons/UIUploadInput.gtmpl"
)
public class UIUploadInput extends UIFormUploadInput {
  
  private String refreshUploadAction;

  public UIUploadInput(String name, String bindingExpression) {
    super(name, bindingExpression);
    setComponentConfig(UIUploadInput.class, null);
  }

  public UIUploadInput(String name, String bindingExpression, int limit) {
    super(name, bindingExpression, limit);
    setComponentConfig(UIUploadInput.class, null);
  }

  public String getRefreshUploadAction() {
    return refreshUploadAction;
  }

  public void setRefreshUploadAction(String refreshUploadAction) {
    this.refreshUploadAction = refreshUploadAction;
  }
  
}
