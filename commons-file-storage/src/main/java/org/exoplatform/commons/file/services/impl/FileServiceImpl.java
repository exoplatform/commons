package org.exoplatform.commons.file.services.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.file.resource.ResourceProvider;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.file.services.util.FileChecksum;
import org.exoplatform.commons.file.storage.dao.DeletedFileDAO;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.dao.NameSpaceDAO;
import org.exoplatform.commons.file.storage.entity.DeletedFileEntity;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * File Service which stores the file metadata in a database, and uses a
 * ResourceProvider to store the file binary.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public class FileServiceImpl implements FileService {

  private static final Log    LOG                = ExoLogger.getLogger(FileServiceImpl.class);

  // TODO isolate DAO calls in a data storage layer, so service layer never
  // deals with Entity object
  private static final String Algorithm_PARAM    = "Algorithm";

  private FileInfoDAO         fileInfoDAO;

  private NameSpaceDAO        nameSpaceDAO;
  
  private DeletedFileDAO      deletedFileDAO;

  private ResourceProvider    resourceProvider;

  private FileChecksum        fileChecksum;

  private static long         defaultNameSpaceId = -1;

  public FileServiceImpl(FileInfoDAO fileInfoDAO,
                         NameSpaceDAO nameSpaceDAO,
                         DeletedFileDAO deletedFileDAO,
                         ResourceProvider resourceProvider,
                         InitParams initParams)
      throws Exception {
    this.fileInfoDAO = fileInfoDAO;
    this.resourceProvider = resourceProvider;
    this.nameSpaceDAO = nameSpaceDAO;
    this.deletedFileDAO = deletedFileDAO;

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
   * @param id file id
   * @return file info
   * @throws IOException ignals that an I/O exception of some sort has occurred.
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
   * @param id file id
   * @return fileItem
   * @throws Exception ignals that an I/O exception of some sort has occurred.
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
   * @param file file item
   * @return updated file item
   * @throws IOException ignals that an I/O exception of some sort has occurred.
   */
  @Override
  public FileItem writeFile(FileItem file) throws FileStorageException, IOException {
    if (file.getFileInfo() == null || StringUtils.isEmpty(file.getFileInfo().getChecksum())) {
      throw new IllegalArgumentException("Checksum is required to persist the binary");
    }
    FileInfo fileInfo = file.getFileInfo();
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
      FileStorageTransaction transaction = new FileStorageTransaction(fileInfoEntity);
      FileInfoEntity createdFileInfoEntity= transaction.towPhaseCommit(2, file.getAsStream()); 
      if (createdFileInfoEntity != null) {
        fileInfo.setId(createdFileInfoEntity.getId());
        file.setFileInfo(fileInfo);
        return file;
      }
    return null;
  }

  public FileItem updateFile(FileItem file) throws FileStorageException, IOException {
    if (file.getFileInfo() == null || StringUtils.isEmpty(file.getFileInfo().getChecksum())) {
      throw new IllegalArgumentException("Checksum is required to persist the binary");
    }
    FileInfo fileInfo = file.getFileInfo();
    NameSpaceEntity nSpace;
    if (fileInfo.getNameSpace() != null && !fileInfo.getNameSpace().isEmpty()) {
      nSpace = nameSpaceDAO.getNameSpaceByName(fileInfo.getNameSpace());
    } else {
      nSpace = nameSpaceDAO.find(defaultNameSpaceId);
    }
    FileInfoEntity fileInfoEntity = new FileInfoEntity(fileInfo.getId(),
            fileInfo.getName(),
            fileInfo.getMimetype(),
            fileInfo.getSize(),
            fileInfo.getUpdatedDate(),
            fileInfo.getUpdater(),
            fileInfo.getChecksum(),
            fileInfo.isDeleted());
    fileInfoEntity.setNameSpaceEntity(nSpace);
    FileStorageTransaction transaction = new FileStorageTransaction(fileInfoEntity);
    FileInfoEntity createdFileInfoEntity= transaction.towPhaseCommit(0, file.getAsStream());
    if (createdFileInfoEntity != null) {
      fileInfo.setId(createdFileInfoEntity.getId());
      file.setFileInfo(fileInfo);
      return file;
    }
    return null;
  }

  /**
   * Delete file with the given id The file is not physically deleted, it is
   * only a logical deletion
   * 
   * @param id Id of the file to delete
   * @return  file Info
   */
  @Override
  public FileInfo deleteFile(long id) {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);
    FileInfo fileInfo = null;
    if (fileInfoEntity != null) {
      fileInfoEntity.setDeleted(true);
      FileInfoEntity updateFileInfoEntity = fileInfoDAO.update(fileInfoEntity);
      fileInfo = new FileInfo(updateFileInfoEntity.getId(),
                              updateFileInfoEntity.getName(),
                              updateFileInfoEntity.getMimetype(),
                              updateFileInfoEntity.getNameSpaceEntity().getName(),
                              updateFileInfoEntity.getSize(),
                              updateFileInfoEntity.getUpdatedDate(),
                              updateFileInfoEntity.getUpdater(),
                              updateFileInfoEntity.getChecksum(),
                              updateFileInfoEntity.isDeleted());
    }
    return fileInfo;
  }

  private class FileStorageTransaction {
    /**
     * Update Operation.
     */
    final int              UPDATE = 0;

    /**
     * Remove Operation.
     */
    final int              REMOVE = 1;

    /**
     * Insert Operation.
     */
    final int              INSERT = 2;

    private FileInfoEntity fileInfoEntity;

    public FileStorageTransaction(FileInfoEntity fileInfoEntity) {
      this.fileInfoEntity = fileInfoEntity;
    }

    public FileInfoEntity towPhaseCommit(int operation, InputStream inputStream) throws FileStorageException {
      FileInfoEntity createdFileInfoEntity = null;
      if (operation == INSERT) {
        try {
          resourceProvider.put(fileInfoEntity.getChecksum(), inputStream);
          if (resourceProvider.exists(fileInfoEntity.getChecksum())) {
            createdFileInfoEntity = fileInfoDAO.create(fileInfoEntity);
            return createdFileInfoEntity;
          } else {
            throw new FileStorageException("Error while writing file " + fileInfoEntity.getName());
          }
        } catch (Exception e) {
          try {
            resourceProvider.remove(fileInfoEntity.getChecksum());
          } catch (IOException e1) {
            LOG.error("Error while rollback writing file");
          }
          throw new FileStorageException("Error while writing file " + fileInfoEntity.getName(), e);
        }

      } else if (operation == REMOVE) {
        fileInfoEntity.setDeleted(true);
        fileInfoDAO.update(fileInfoEntity);
      } else if (operation == UPDATE) {
        try {
          FileInfoEntity old = fileInfoDAO.find(fileInfoEntity.getId());
          if(old == null || old.getChecksum().isEmpty() || !old.getChecksum().equals(fileInfoEntity.getChecksum())) {
            resourceProvider.put(fileInfoEntity.getChecksum(), inputStream);
          }
          if(old != null && old.getChecksum()!= null && !old.getChecksum().isEmpty())
          {
            DeletedFileEntity deletedFileEntity=new DeletedFileEntity();
            deletedFileEntity.setChecksum(old.getChecksum());
            deletedFileEntity.setFileInfoEntity(old);
            deletedFileEntity.setDeletedDate(new Date());
            deletedFileDAO.create(deletedFileEntity);
          }
          if (resourceProvider.exists(fileInfoEntity.getChecksum())) {
            createdFileInfoEntity = fileInfoDAO.update(fileInfoEntity);
            return createdFileInfoEntity;
          } else {
            throw new FileStorageException("Error while writing file " + fileInfoEntity.getName());
          }
        } catch (Exception e) {
          try {
            resourceProvider.remove(fileInfoEntity.getChecksum());
          } catch (IOException e1) {
            LOG.error("Error while rollback writing file");
          }
          throw new FileStorageException("Error while writing file " + fileInfoEntity.getName(), e);
        }
      }
      return null;
    }
  }
}
