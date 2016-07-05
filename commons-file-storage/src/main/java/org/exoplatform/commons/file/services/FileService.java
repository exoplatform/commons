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
  /**
   * Get only the file info of the given id
   *
   * @param id file id
   * @return file info
   * @throws IOException ignals that an I/O exception of some sort has occurred.
   */
  public FileInfo getFileInfo(long id) throws IOException;

  /**
   * Get the file (info + binary) of the given id
   *
   * @param id file id
   * @return fileItem
   * @throws Exception signals that an I/O exception of some sort has occurred.
   */
  public FileItem getFile(long id) throws Exception;

  /**
   * Store the file using the provided DAO and binary provider. This method is
   * transactional, meaning that if the write of the info or of the binary
   * fails, nothing must be persisted.
   *
   * @param file file item
   * @return updated file item
   * @throws IOException signals that an I/O exception of some sort has occurred.
   */
  public FileItem writeFile(FileItem file) throws FileStorageException, IOException;

  /**
   * Update the stored file using the provided DAO and binary provider. This method is
   * transactional, meaning that if the write of the info or of the binary
   * fails, nothing must be persisted.
   *
   * @param file file item
   * @return updated file item
   * @throws IOException signals that an I/O exception of some sort has occurred.
   */
  public FileItem updateFile(FileItem file) throws FileStorageException, IOException;

  /**
   * Delete file with the given id The file is not physically deleted, it is
   * only a logical deletion
   *
   * @param id Id of the file to delete
   * @return  file Info
   */
  public FileInfo deleteFile(long id);
}
