package org.exoplatform.commons.file.storage;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for File Information
 */
@Entity(name = "FileInfoEntity")
@ExoEntity
@Table(name = "COMMONS_FILES")
public class FileInfoEntity {

  @Id
  @Column(name = "FILE_ID")
  @SequenceGenerator(name="SEQ_COMMONS_FILES_FILE_ID", sequenceName="SEQ_COMMONS_FILES_FILE_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_COMMONS_FILES_FILE_ID")
  private long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "MIMETYPE")
  private String mimetype;

  @Column(name = "SIZE")
  private long size;

  @Column(name = "UPDATED_DATE")
  private Date updatedDate;

  @Column(name = "UPDATER")
  private String updater;

  @Column(name = "CHECKSUM")
  private String checksum;

  @Column(name = "DELETED")
  private boolean deleted;

  public FileInfoEntity() {
  }

  public FileInfoEntity(String name, String mimetype, long size, Date updatedDate, String updater, String checksum, boolean deleted) {
    this.name = name;
    this.mimetype = mimetype;
    this.size = size;
    this.updatedDate = updatedDate;
    this.updater = updater;
    this.checksum = checksum;
    this.deleted = deleted;
  }

  public FileInfoEntity(long id, String name, String mimetype, long size, Date updatedDate, String updater, String checksum, boolean deleted) {
    this(name, mimetype, size, updatedDate, updater, checksum, deleted);
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getUpdater() {
    return updater;
  }

  public void setUpdater(String updater) {
    this.updater = updater;
  }

  public String getChecksum() {
    return checksum;
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
