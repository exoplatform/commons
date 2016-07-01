package org.exoplatform.commons.file.model;

import java.util.Date;

/**
 * File information
 */
public class FileInfo {
  protected Long id;
  protected String name;
  protected String mimetype;
  protected String nameSpace;
  protected long size;
  protected Date updatedDate;
  protected String updater;
  protected String checksum;
  protected boolean deleted;

  public FileInfo(Long id, String name, String mimetype, String nameSpace, long size, Date updatedDate, String updater, String checksum, boolean deleted) {
    this.id = id;
    this.name = name;
    this.mimetype = mimetype;
    this.nameSpace = nameSpace;
    this.size = size;
    this.updatedDate = updatedDate;
    this.updater = updater;
    this.checksum = checksum;
    this.deleted = deleted;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getMimetype() {
    return mimetype;
  }

  public long getSize() {
    return size;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public String getUpdater() {
    return updater;
  }

  public String getChecksum() {
    return checksum;
  }
  public String getNameSpace() {
    return nameSpace;
  }
  public void setNameSpace(String nameSpace) {
    this.nameSpace = nameSpace;
  }
  public void setId(Long id) {
    this.id = id;
  }


  public void setName(String name) {
    this.name = name;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public void setUpdater(String updater) {
    this.updater = updater;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
