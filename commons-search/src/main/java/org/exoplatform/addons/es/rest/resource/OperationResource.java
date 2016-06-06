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
@ApiModel(value="An Indexing Operation Resource")
public class OperationResource  implements Serializable {

  private String entityType;
  private String entityId;
  private String operation;

  public OperationResource() {
  }

  public OperationResource(String entityType, String entityId, String operation) {
    this.entityType = entityType;
    this.entityId = entityId;
    this.operation = operation;
  }

  @ApiModelProperty(value = "The Entity Type")
  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @ApiModelProperty(value = "The Entity Id", notes = "Mandatory if the operation is index/reindex/unindex")
  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  @ApiModelProperty(value = "The Indexing operation", allowableValues = "init,index,reindex,unindex,reindexAll,unindexAll")
  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }
}

