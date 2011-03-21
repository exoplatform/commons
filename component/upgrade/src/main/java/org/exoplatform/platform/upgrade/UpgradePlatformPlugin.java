package org.exoplatform.platform.upgrade;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class UpgradePlatformPlugin extends BaseComponentPlugin {

  /**
   * proceed to the transparent upgrade, this method will be called if the Platform version has changed, which means, it will be called once the Platform Version stored in the JCR and the one declared in PlatformInfo are different
   * 
   * @param oldVersion
   *          the old version that is stored in the JCR
   * @param newVersion
   *          the new version read from PlatformInfo Service
   */
  public abstract void processUpgrade(String oldVersion, String newVersion);

  /**
   * Add node's version with a label
   * 
   * @param templateNode
   *          the node that will be versioned
   * @param label
   *          the label to apply to the new created version
   * @throws RepositoryException
   * @throws NoSuchNodeTypeException
   * @throws VersionException
   * @throws ConstraintViolationException
   * @throws LockException
   * @throws AccessDeniedException
   * @throws ItemExistsException
   * @throws InvalidItemStateException
   * @throws ReferentialIntegrityException
   * @throws UnsupportedRepositoryOperationException
   */
  protected void addTemplateVersion(Node nodeToVersion, String versionLabel) throws RepositoryException, NoSuchNodeTypeException,
      VersionException, ConstraintViolationException, LockException, AccessDeniedException, ItemExistsException,
      InvalidItemStateException, ReferentialIntegrityException, UnsupportedRepositoryOperationException {
    if (!nodeToVersion.isNodeType(UpgradePlatformService.MIX_VERSIONABLE)) {
      nodeToVersion.addMixin(UpgradePlatformService.MIX_VERSIONABLE);
      nodeToVersion.save();
      nodeToVersion.getSession().save();
      nodeToVersion.getSession().refresh(true);
    }
    if (nodeToVersion.isCheckedOut()) {
      Version version = nodeToVersion.checkin();
      nodeToVersion.getVersionHistory().addVersionLabel(version.getName(), versionLabel, false);
    }
    nodeToVersion.checkout();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof UpgradePlatformPlugin) {
      return this.getName().equals(((UpgradePlatformPlugin) obj).getName());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return this.getName().hashCode();
  }

}
