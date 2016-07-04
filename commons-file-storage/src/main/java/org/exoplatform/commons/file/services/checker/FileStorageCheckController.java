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
package org.exoplatform.commons.file.services.checker;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.resource.ResourceProvider;
import org.exoplatform.commons.file.services.job.FileStorageCleanJob;
import org.exoplatform.commons.file.storage.DataStorage;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

import java.io.*;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * File Storage Check Controller allows check file Storage consistency: Created
 * by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
@Managed
@NameTemplate(@Property(key = "service", value = "FileStorageCheckController"))
@ManagedDescription("File Storage Check consistency")
public class FileStorageCheckController implements Startable {
  protected static Log       LOG                           = ExoLogger.getLogger(FileStorageCheckController.class);

  public static final String REPORT_CONSISTENT_MESSAGE     = "File Storage data is consistent";

  public static final String REPORT_NOT_CONSISTENT_MESSAGE = "File Storage data is NOT consistent";

  private static final int   pageSize                      = 20;

  private ResourceProvider           resourceProvider;

  private DataStorage dataStorage;

  public FileStorageCheckController(DataStorage dataStorage, ResourceProvider resourceProvider) {
    this.dataStorage = dataStorage;
    this.resourceProvider = resourceProvider;
  }

  @Managed
  @ManagedDescription("Check File Storage consistency. ")
  public String checkFileStorage() {
    boolean defaultState = FileStorageCleanJob.isEnabled().get();
    ;
    try {
      Report report = new Report();
      if (FileStorageCleanJob.isEnabled().get()) {
        FileStorageCleanJob.setEnabled(false);
      }
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Boolean>() {
        public Boolean run() {
          try {
            LOG.info("Start File Storage Check Consistency : ");
            boolean isConsistent = true;
            int offset = 0;
            boolean hasNext = false;
            while (hasNext) {
              List<FileInfo> list = dataStorage.getAllFilesInfo(offset, pageSize);

              if (list == null && list.isEmpty()) {
                break;
              }
              if (list.size() < pageSize) {
                hasNext = false;
              }
              for (FileInfo fileInfo : list) {
                String checksum = fileInfo.getChecksum();
                if (checksum != null && !checksum.isEmpty()) {
                  if (!resourceProvider.exists(checksum)) {
                    isConsistent = false;
                    report.writeLine("File not exist in file storage File ID : " + fileInfo.getId() + " File name : "
                        + fileInfo.getName() + " , Path : " + resourceProvider.getFilePath(fileInfo.getChecksum()));
                  }
                } else {
                  isConsistent = false;
                  report.writeLine("File metaData with empty checksum File ID : " + fileInfo.getId() + " File name : "
                      + fileInfo.getName() + " , Path : ");
                }
              }
              offset+=pageSize;
            }
            if (isConsistent) {
              report.writeLine(REPORT_CONSISTENT_MESSAGE);
              LOG.info("Finish File Storage Check Consistency : " + REPORT_CONSISTENT_MESSAGE);
            } else {
              report.writeLine(REPORT_NOT_CONSISTENT_MESSAGE);
              LOG.info("Finish File Storage Check Consistency : " + REPORT_NOT_CONSISTENT_MESSAGE);
            }
          } catch (Exception e) {
            try {
              report.writeLine("Processing File Storage Check Consistency Error ");
              report.writeStackTrace(e);
            } catch (IOException e1) {
              LOG.error(e1.getMessage());
            }
            LOG.error(e.getMessage());
          }
          return true;
        }
      });
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      return "Failed Operation";
    } finally {
      FileStorageCleanJob.setEnabled(defaultState);
    }
    return "Success Operation";
  }

  @Managed
  @ManagedDescription("Check File Storage consistency. ")
  public String RepairFileStorage() {
    try {
      // TODO Repair file Storage data
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      return "Failed";
    }
    return "Unsupported Operation";
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  private class Report {
    private static final String DELIMITER = "\n";

    private Writer              writer;

    private String              reportPath;

    public Report() throws IOException {
      String reportPathRoot = PropertyManager.getProperty("java.io.tmpdir");
      final File reportFile = new File(reportPathRoot,
                                       "report-filesStorage-" + new SimpleDateFormat("dd-MMM-yy-HH-mm").format(new Date())
                                           + ".txt");

      SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<Void>() {
        public Void run() throws IOException {
          reportPath = reportFile.getAbsolutePath();
          writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportPath)));

          return null;
        }
      });

    }

    private void writeLine(String message) throws IOException {
      writer.write(message);
      writer.write(DELIMITER);
      writer.flush();
    }

    private void writeStackTrace(Throwable e) throws IOException {
      writeLine(e.getMessage());
      writeLine(e.toString());
      StackTraceElement[] trace = e.getStackTrace();
      for (int i = 0; i < trace.length; i++) {
        writeLine("\tat " + trace[i]);
      }

      Throwable ourCause = e.getCause();
      if (ourCause != null) {
        writeLine("Cause:");
        writeStackTrace(ourCause);
      }
    }

  }
}
