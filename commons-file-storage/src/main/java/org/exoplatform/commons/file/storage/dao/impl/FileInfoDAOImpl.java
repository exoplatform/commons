/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.file.storage.dao.impl;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
public class FileInfoDAOImpl extends GenericDAOJPAImpl<FileInfoEntity, Long> implements FileInfoDAO {

  private static org.exoplatform.services.log.Log Log = ExoLogger.getLogger(FileInfoDAOImpl.class);

  @Override
  public List<FileInfoEntity> findDeletedFiles(Date date) {
    TypedQuery<FileInfoEntity> query = getEntityManager().createNamedQuery("fileEntity.findDeletedFiles", FileInfoEntity.class)
                                                         .setParameter("updatedDate", date);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
        Log.error("Unable to get deleted file"+ e.getMessage());
    }
    return null;
  }

    @Override
    public void deleteFileInfoByNameSpace(NameSpaceEntity nameSpaceEntity) {
        TypedQuery<FileInfoEntity> query = getEntityManager().createNamedQuery("FileInfo.getFileInfoByNameSpace", FileInfoEntity.class)
                .setParameter("nameSpaceID", nameSpaceEntity.getId());
        try {
             query.getSingleResult();
        } catch (NoResultException e) {

        }
    }
}
