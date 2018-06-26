package org.exoplatform.commons.upgrade;

import javax.jcr.*;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.transaction.TransactionService;

/**
 * This Upgrade Plugin will Delete a chosen JCR Structure
 */
public class DeleteJCRFolderUpgradePlugin extends UpgradeProductPlugin {
  private static final Log         LOG                        = ExoLogger.getLogger(DeleteJCRFolderUpgradePlugin.class);

  protected static final int       ONE_DAY_IN_SECONDS         = 86400;

  protected static final long      ONE_DAY_IN_MS              = 86400000L;

  final public static String       FOLDER_TO_DELETE_WORKSPACE = "workspace";

  final public static String       FOLDER_TO_DELETE_PATH      = "path";

  final private RepositoryService  repositoryService;

  final private TransactionService transactionService;

  final private String             workspace;

  final private String             parentPath;

  private long                     defaultJCRSessionTimeout;

  private int                      deletedFoldersCount        = 0;

  public DeleteJCRFolderUpgradePlugin(RepositoryService repositoryService,
                                      TransactionService transactionService,
                                      InitParams initParams) {
    super(initParams);
    this.repositoryService = repositoryService;
    this.transactionService = transactionService;
    if (!initParams.containsKey(FOLDER_TO_DELETE_WORKSPACE)) {
      throw new IllegalStateException("Workspace is mandatory");
    }
    if (!initParams.containsKey(FOLDER_TO_DELETE_PATH)) {
      throw new IllegalStateException("Path is mandatory");
    }
    workspace = initParams.getValueParam(FOLDER_TO_DELETE_WORKSPACE).getValue();
    parentPath = initParams.getValueParam(FOLDER_TO_DELETE_PATH).getValue();
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    increaseCurrentTransactionTimeOut();
    try {
      Session session = getSession(null);
      if (!session.itemExists(parentPath)) {
        return;
      }
      Node parentNode = (Node) session.getItem(parentPath);
      removeRecursively(parentNode);
      parentNode.remove();
      session.save();
      LOG.info("Parent folder {}:{} is removed successfully. {} folders was removed.",
               workspace,
               parentPath,
               deletedFoldersCount);
    } catch (Exception e) {
      throw new RuntimeException("An error occurred while deleting JCR folder structure " + workspace + ":" + parentPath, e);
    } finally {
      restoreDefaultTransactionTimeOut();
    }
  }

  private Session getSession(Session session) throws RepositoryException {
    if (session == null || !((SessionImpl) session).isLive()) {
      try {
        session.save();
      } catch (Exception e) {
        LOG.trace("Error committing transaction", e);
      }
      session = repositoryService.getCurrentRepository().getSystemSession(workspace);
      ((SessionImpl) session).setTimeout(ONE_DAY_IN_MS);
    }
    return session;
  }

  private void removeRecursively(Node parentNode) throws RepositoryException {
    if (deletedFoldersCount > 0 && deletedFoldersCount % 100 == 0) {
      parentNode.getSession().save();
      LOG.info("{} folders are removed", deletedFoldersCount);
    }
    if (parentNode.hasNodes()) {
      NodeIterator nodes = parentNode.getNodes();
      while (nodes.hasNext()) {
        deletedFoldersCount++;
        Node childNode = nodes.nextNode();
        removeRecursively(childNode);
        childNode.remove();
      }
    }
  }

  private void increaseCurrentTransactionTimeOut() {
    try {
      transactionService.setTransactionTimeout(ONE_DAY_IN_SECONDS);
    } catch (Exception e) {
      LOG.warn("Cannot Change Transaction timeout", e);
    }
    try {
      ManageableRepository repo = repositoryService.getCurrentRepository();
      if (defaultJCRSessionTimeout == 0) {
        defaultJCRSessionTimeout = repo.getConfiguration().getSessionTimeOut();
      }
      repo.getConfiguration().setSessionTimeOut(ONE_DAY_IN_MS);
    } catch (Exception e) {
      LOG.warn("Cannot Change JCR Session timeout", e);
    }
  }

  private void restoreDefaultTransactionTimeOut() {
    if (defaultJCRSessionTimeout == 0) {
      return;
    }
    try {
      ManageableRepository repo = repositoryService.getCurrentRepository();
      repo.getConfiguration().setSessionTimeOut(defaultJCRSessionTimeout);
    } catch (Exception e) {
      LOG.warn("Cannot Change JCR Session timeout", e);
    }
  }

}
