package org.exoplatform.commons.file.services.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.file.fileSystem.BinaryProvider;
import org.exoplatform.commons.file.fileSystem.ResourceProvider;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.util.FileChecksum;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.dao.NameSpaceDAO;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * File Service which stores the file metadata in a database, and uses a
 * BinaryProvider to store the file binary. Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public class FileServiceImpl implements FileService {

  private static final Log    LOG                = ExoLogger.getLogger(FileServiceImpl.class);

  // TODO isolate DAO calls in a data storage layer, so service layer never
  // deals with Entity object
  private static final String Algorithm_PARAM    = "Algorithm";

  private FileInfoDAO         fileInfoDAO;

  private NameSpaceDAO        nameSpaceDAO;

  private ResourceProvider    resourceProvider;

  private FileChecksum        fileChecksum;

  private static long         defaultNameSpaceId = -1;

  public FileServiceImpl(FileInfoDAO fileInfoDAO,
                         NameSpaceDAO nameSpaceDAO,
                         ResourceProvider resourceProvider,
                         InitParams initParams)
      throws Exception {
    this.fileInfoDAO = fileInfoDAO;
    this.resourceProvider = resourceProvider;
    this.nameSpaceDAO = nameSpaceDAO;

    ValueParam algorithmValueParam = null;
    if (initParams != null) {
      algorithmValueParam = initParams.getValueParam(Algorithm_PARAM);
    }

    if (algorithmValueParam == null) {
      this.fileChecksum = new FileChecksum();
    } else {
      this.fileChecksum = new FileChecksum(algorithmValueParam.getValue());
    }

    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.getNameSpaceByName(NameSpaceServiceImpl.getDefaultNameSpace());
    if (nameSpaceEntity != null) {
      defaultNameSpaceId = nameSpaceEntity.getId();
    }
  }

  /**
   * Get only the file info of the given id
   * 
   * @param id
   * @return
   * @throws IOException
   */
  @Override
  public FileInfo getFileInfo(long id) throws IOException {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);

    if (fileInfoEntity == null) {
      return null;
    }

    FileInfo fileInfo = new FileInfo(fileInfoEntity.getId(),
                                     fileInfoEntity.getName(),
                                     fileInfoEntity.getMimetype(),
                                     fileInfoEntity.getNameSpaceEntity().getName(),
                                     fileInfoEntity.getSize(),
                                     fileInfoEntity.getUpdatedDate(),
                                     fileInfoEntity.getUpdater(),
                                     fileInfoEntity.getChecksum(),
                                     fileInfoEntity.isDeleted());

    return fileInfo;
  }

  /**
   * Get the file (info + binary) of the given id
   * 
   * @param id
   * @return
   * @throws IOException
   */
  @Override
  public FileItem getFile(long id) throws Exception {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);

    if (fileInfoEntity == null) {
      return null;
    }

    FileInfo fileInfo = new FileInfo(fileInfoEntity.getId(),
                                     fileInfoEntity.getName(),
                                     fileInfoEntity.getMimetype(),
                                     fileInfoEntity.getNameSpaceEntity().getName(),
                                     fileInfoEntity.getSize(),
                                     fileInfoEntity.getUpdatedDate(),
                                     fileInfoEntity.getUpdater(),
                                     fileInfoEntity.getChecksum(),
                                     fileInfoEntity.isDeleted());
    if (StringUtils.isEmpty(fileInfo.getChecksum())) {
      return null;
    }
    FileItem fileItem = new FileItem(fileInfo, null);
    InputStream inputStream = resourceProvider.getStream(fileInfo.getChecksum());
    fileItem.setInputStream(inputStream);

    return fileItem;
  }

  /**
   * Store the file using the provided DAO and binary provider. This method is
   * transactional, meaning that if the write of the info or of the binary
   * fails, nothing must be persisted.
   * 
   * @param file
   * @return
   * @throws IOException
   */
  @Override
  public FileItem writeFile(FileItem file) throws IOException {
    if (file.getFileInfo() == null || StringUtils.isEmpty(file.getFileInfo().getChecksum())) {
      throw new IllegalArgumentException("Checksum is required to persist the binary");
    }
    FileInfo fileInfo = file.getFileInfo();
    resourceProvider.put(fileInfo.getChecksum(), file.getAsStream());
    try {
      NameSpaceEntity nSpace;
      if (fileInfo.getNameSpace() != null && !fileInfo.getNameSpace().isEmpty()) {
        nSpace = nameSpaceDAO.getNameSpaceByName(fileInfo.getNameSpace());
      } else {
        nSpace = nameSpaceDAO.find(defaultNameSpaceId);
      }
      FileInfoEntity fileInfoEntity = new FileInfoEntity(fileInfo.getName(),
                                                         fileInfo.getMimetype(),
                                                         fileInfo.getSize(),
                                                         fileInfo.getUpdatedDate(),
                                                         fileInfo.getUpdater(),
                                                         fileInfo.getChecksum(),
                                                         fileInfo.isDeleted());
      fileInfoEntity.setNameSpaceEntity(nSpace);
      FileInfoEntity createdFileInfoEntity = fileInfoDAO.create(fileInfoEntity);
      fileInfo.setId(createdFileInfoEntity.getId());
      file.setFileInfo(fileInfo);
      return file;
    } catch (Exception e) {
      LOG.error("Error while writing file " + file.getFileInfo().getId() + " - Cause : " + e.getMessage(), e);
      // something went wrong with the database update, rollback the file write
      resourceProvider.remove(Long.toString(fileInfo.getId()));
    }
    return null;
  }

  /**
   * Delete file with the given id The file is not physically deleted, it is
   * only a logical deletion
   * 
   * @param id Id of the file to delete
   * @throws IOException
   */
  @Override
  public void deleteFile(long id) throws IOException {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);
    if (fileInfoEntity != null) {
      fileInfoEntity.setDeleted(true);
      fileInfoDAO.update(fileInfoEntity);
    }
  }
}
