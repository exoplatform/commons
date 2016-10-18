package org.exoplatform.commons.file.storage.dao.impl;

import org.exoplatform.commons.file.storage.dao.FileBinaryDAO;
import org.exoplatform.commons.file.storage.entity.FileBinaryEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * Data Access Object layer for binary data files.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public class FileBinaryDAOImpl extends GenericDAOJPAImpl<FileBinaryEntity, Long> implements FileBinaryDAO {
    @Override
    public FileBinaryEntity findFileBinaryByName(String name) {
        TypedQuery<FileBinaryEntity> query = getEntityManager().createNamedQuery("FileBinaryEntity.findByName", FileBinaryEntity.class)
                .setParameter("name", name);
        try{
            return query.getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }
}
