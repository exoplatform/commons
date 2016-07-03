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
package org.exoplatform.commons.file.storage.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Deleted Files.
 *
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
@Entity(name = "DeletedFileEntity")
@ExoEntity
@Table(name = "COMMONS_ORPHAN_FILES")

@NamedQueries(
        @NamedQuery(name = "deletedEntity.findDeletedFiles", query = "SELECT t FROM DeletedFileEntity t WHERE t.deletedDate < :deletedDate")
)
public class OrphanFileEntity {
    @Id
    @Column(name = "ID")
    @SequenceGenerator(name="SEQ_COMMONS_ORPHAN_FILES_ID", sequenceName="SEQ_COMMONS_ORPHAN_FILES_ID")
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_COMMONS_ORPHAN_FILES_ID")
    private long id;

    @ManyToOne
    @JoinColumn(name = "FILE_ID")
    private FileInfoEntity fileInfoEntity;

    @Column(name = "CHECKSUM")
    private String checksum;

    @Column(name = "DELETED_DATE")
    private Date deletedDate;

    public OrphanFileEntity() {
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public FileInfoEntity getFileInfoEntity() {
        return fileInfoEntity;
    }

    public void setFileInfoEntity(FileInfoEntity fileInfoEntity) {
        this.fileInfoEntity = fileInfoEntity;
    }

    public OrphanFileEntity(long id, String checksum, Date deletedDate) {
        this.id = id;
        this.checksum = checksum;
        this.deletedDate = deletedDate;
    }

    public OrphanFileEntity(long id, long fileId, String checksum, Date deletedDate) {
        this(fileId, checksum, deletedDate);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
