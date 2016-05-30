package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;

import java.io.IOException;

/**
 *
 */
public interface FileService {

  public FileInfo getFileInfo(long id) throws IOException;

  public FileItem getFile(long id) throws IOException;

  public FileItem writeFile(FileItem file) throws IOException;

  public void deleteFile(long id) throws IOException;
}
