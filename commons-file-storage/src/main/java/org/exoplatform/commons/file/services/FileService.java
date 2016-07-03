package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
public interface FileService {

  public FileInfo getFileInfo(long id) throws IOException;

  public FileItem getFile(long id) throws Exception;

  public FileItem writeFile(FileItem file) throws FileStorageException, IOException;

  public FileItem updateFile(FileItem file) throws FileStorageException, IOException;

  public FileInfo deleteFile(long id);
}
