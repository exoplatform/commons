package org.exoplatform.commons.file.model;

import org.apache.commons.io.IOUtils;
import org.exoplatform.commons.file.fileSystem.TreeFile;
import org.exoplatform.commons.file.fileSystem.TreeFileIOChannel;
import org.exoplatform.commons.file.services.util.FileChecksum;
import org.exoplatform.commons.utils.PrivilegedFileHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Object representing a file : information + binary
 */
public class FileItem {
  protected FileInfo fileInfo;

  /**
   * Object representing a file : information + binary
   */
  protected byte[]   data;

  public FileItem(FileInfo fileInfo, InputStream inputStream) throws Exception {
    this.fileInfo = fileInfo;
    if (inputStream != null) {
      this.data = IOUtils.toByteArray(inputStream);
      setChecksum(new ByteArrayInputStream(data));
    }
  }

  public FileItem(Long id,
                  String name,
                  String mimetype,
                  String nameSpace,
                  long size,
                  Date updatedDate,
                  String updater,
                  boolean deleted,
                  InputStream inputStream)
      throws Exception {
    this.fileInfo = new FileInfo(id, name, mimetype, nameSpace, size, updatedDate, updater, null, deleted);
    if (inputStream != null) {
      this.data = IOUtils.toByteArray(inputStream);
      setChecksum(new ByteArrayInputStream(data));
    }
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getAsStream() throws IOException {
    if (data != null)
      return new ByteArrayInputStream(data);
    else
      return null;
  }

  public void setInputStream(InputStream inputStream) throws IOException {
    if (inputStream != null) {
      this.data = IOUtils.toByteArray(inputStream);
    }
  }

  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  public void setChecksum(InputStream inputStream) throws Exception {
    try {
      if (inputStream != null) {
        String checksum = FileChecksum.getMD5Checksum(inputStream);
        fileInfo.setChecksum(checksum);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
