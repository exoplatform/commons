/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.file.model;

import java.util.Date;

/**
 * Orphan File information
 */
public class OrphanFile {
  private long   id;

  private long   fileId;

  private Date   deletedDate;

  private String checksum;

  public OrphanFile(long fileId, String checksum, Date deletedDate) {
    this.fileId = fileId;
    this.checksum = checksum;
    this.deletedDate = deletedDate;
  }

  public OrphanFile(long id, long fileId, String checksum, Date deletedDate) {
    this.id = id;
    this.fileId = fileId;
    this.checksum = checksum;
    this.deletedDate = deletedDate;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getFileId() {
    return fileId;
  }

  public void setFileId(long fileId) {
    this.fileId = fileId;
  }

  public Date getDeletedDate() {
    return deletedDate;
  }

  public void setDeletedDate(Date deletedDate) {
    this.deletedDate = deletedDate;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }
}
