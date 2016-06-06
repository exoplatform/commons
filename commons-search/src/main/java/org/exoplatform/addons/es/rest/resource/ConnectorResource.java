/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.es.rest.resource;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 12/4/15
 */
@ApiModel(value="An Indexing Connector Resources")
public class ConnectorResource implements Serializable {

  private String type;
  private boolean enable;

  public ConnectorResource() {
  }

  public ConnectorResource(String type, boolean enable) {
    this.type = type;
    this.enable = enable;
  }

  @ApiModelProperty(value = "The Connector Type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @ApiModelProperty(value = "Does the Connector is enable or not", allowableValues = "true,false")
  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }
}

