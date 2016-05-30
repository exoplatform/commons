package org.exoplatform.commons.file.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Object representing a file : information + binary
 */
public class FileItem {
  protected FileInfo fileInfo;
  // TODO find a more efficient way to handle data
  protected byte[] data;

  public FileItem(FileInfo fileInfo, InputStream inputStream) {
    this.fileInfo = fileInfo;
    this.setInputStream(inputStream);
  }

  public FileItem(Long id, String name, String mimetype, long size, Date updatedDate, String updater, boolean deleted, InputStream inputStream) {
    this.fileInfo = new FileInfo(id, name, mimetype, size, updatedDate, updater, null, deleted);
    this.setInputStream(inputStream);
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }

  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  public InputStream getStream() {
    return new ByteArrayInputStream(data);
  }

  public void setInputStream(InputStream inputStream) {
    try {
      if(inputStream != null) {
        this.data = IOUtils.toByteArray(inputStream);
        String checksum = DigestUtils.md5Hex(data);
        fileInfo.setChecksum(checksum);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
