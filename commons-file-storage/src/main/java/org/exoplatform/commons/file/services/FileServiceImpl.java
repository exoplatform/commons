package org.exoplatform.commons.file.services;

import org.apache.commons.io.IOUtils;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.storage.FileInfoDAO;
import org.exoplatform.commons.file.storage.FileInfoEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * File Service which stores the file metadata in a database, and uses a BinaryProvider to store the file binary
 */
public class FileServiceImpl implements FileService {

  private static final Log LOG = ExoLogger.getLogger(FileServiceImpl.class);

  // TODO isolate DAO calls in a data storage layer, so service layer never deals with Entity object
  private FileInfoDAO fileInfoDAO;
  private BinaryProvider binaryProvider;

  public FileServiceImpl(FileInfoDAO fileInfoDAO, BinaryProvider binaryProvider) {
    this.fileInfoDAO = fileInfoDAO;
    this.binaryProvider = binaryProvider;
  }

  /**
   * Get only the file info of the given id
   * @param id
   * @return
   * @throws IOException
   */
  @Override
  public FileInfo getFileInfo(long id) throws IOException {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);

    if(fileInfoEntity == null) {
      return null;
    }

    FileInfo fileInfo = new FileInfo(fileInfoEntity.getId(),
            fileInfoEntity.getName(),
            fileInfoEntity.getMimetype(),
            fileInfoEntity.getSize(),
            fileInfoEntity.getUpdatedDate(),
            fileInfoEntity.getUpdater(),
            fileInfoEntity.getChecksum(),
            fileInfoEntity.isDeleted());

    return fileInfo;
  }

  /**
   * Get the file (info + binary) of the given id
   * @param id
   * @return
   * @throws IOException
   */
  @Override
  public FileItem getFile(long id) throws IOException {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);

    if(fileInfoEntity == null) {
      return null;
    }

    FileInfo fileInfo = new FileInfo(fileInfoEntity.getId(),
            fileInfoEntity.getName(),
            fileInfoEntity.getMimetype(),
            fileInfoEntity.getSize(),
            fileInfoEntity.getUpdatedDate(),
            fileInfoEntity.getUpdater(),
            fileInfoEntity.getChecksum(),
            fileInfoEntity.isDeleted());

    FileItem fileItem = new FileItem(fileInfo, null);

    InputStream inputStream = binaryProvider.readBinary(fileInfo);
    fileItem.setInputStream(inputStream);

    return fileItem;
  }

  /**
   * Store the file using the provided DAO and binary provider.
   * This method is transactional, meaning that if the write of the info or of the binary fails,
   * nothing must be persisted.
   * @param file
   * @return
   * @throws IOException
   */
  @Override
  public FileItem writeFile(FileItem file) throws IOException {
    FileInfo fileInfo = file.getFileInfo();
    binaryProvider.writeBinary(file);
    try {
      FileInfoEntity createdFileInfoEntity = fileInfoDAO.create(new FileInfoEntity(fileInfo.getName(),
              fileInfo.getMimetype(),
              fileInfo.getSize(),
              fileInfo.getUpdatedDate(),
              fileInfo.getUpdater(),
              fileInfo.getChecksum(),
              fileInfo.isDeleted()));
      fileInfo.setId(createdFileInfoEntity.getId());
      file.setFileInfo(fileInfo);
      return file;
    } catch (Exception e) {
      LOG.error("Error while writing file " + file.getFileInfo().getId() + " - Cause : " + e.getMessage(), e);
      // something went wrong with the database update, rollback the file write
      binaryProvider.deleteBinary(fileInfo);
    }
    return null;
  }

  /**
   * Delete file with the given id
   * The file is not physically deleted, it is only a logical deletion
   * @param id Id of the file to delete
   * @throws IOException
   */
  @Override
  public void deleteFile(long id) throws IOException {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);
    if(fileInfoEntity != null) {
      fileInfoEntity.setDeleted(true);
      fileInfoDAO.update(fileInfoEntity);
    }
  }
}
