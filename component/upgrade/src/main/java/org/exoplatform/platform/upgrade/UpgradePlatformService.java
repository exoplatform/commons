package org.exoplatform.platform.upgrade;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.component.product.ProductInformations;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradePlatformService implements Startable {
  private static final String DEFAULT_REPOSITORY = "repository";

  private static final String FIRST_VERSION_LABEL = "FirstVersion";

  private static final Log log = ExoLogger.getLogger(UpgradePlatformService.class);

  /**
   * Constant that will be used in nodeHierarchyCreator.getJcrPath: it represents the Application data root node Alias
   */
  public static final String EXO_APPLICATIONS_DATA_NODE_ALIAS = "exoApplicationDataNode";

  /**
   * MixinType that will activate the versioning on a selected node
   */
  public static final String MIX_VERSIONABLE = "mix:versionable";

  /**
   * Service application data node name
   */
  public static final String UPGRADE_PLATFORM_SERVICE_NODE_NAME = "UpgradePlatformService";

  /**
   * node name where the Platform version declaration is
   */
  public static final String PLATFORM_VERSION_DECLARATION_NODE_NAME = "platformVersionDeclarationNode";

  private String workspaceName = null;
  private Set<UpgradePlatformPlugin> upgradePlugins = new HashSet<UpgradePlatformPlugin>();
  private String applicationDataRootNodePath = null;
  private String currentVersion = null;
  private String oldVersion = null;
  private RepositoryService repositoryService = null;
  private SessionProviderService sessionProviderService = null;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param repositoryService
   * @param sessionProviderService
   * @param nodeHierarchyCreator
   */
  public UpgradePlatformService(RepositoryService repositoryService, SessionProviderService sessionProviderService,
      NodeHierarchyCreator nodeHierarchyCreator, ProductInformations productInformations, InitParams initParams)
      throws RepositoryException, RepositoryConfigurationException {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;

    if (initParams.containsKey("working.worspace.name")) {
      workspaceName = initParams.getValueParam("working.worspace.name").getValue();
    }
    applicationDataRootNodePath = nodeHierarchyCreator.getJcrPath(EXO_APPLICATIONS_DATA_NODE_ALIAS);
    if (applicationDataRootNodePath.indexOf("/") == 0) {
      applicationDataRootNodePath = applicationDataRootNodePath.replaceFirst("/", "");
    }
    currentVersion = productInformations.getVersion();
    if (log.isDebugEnabled()) {
      log.debug("Constructor: Platform current version number = " + currentVersion);
    }
  }

  /**
   * Method called by eXo Kernel to inject upgrade plugins
   * 
   * @param upgradePLatformPlugin
   */
  public void addUpgradePlugin(UpgradePlatformPlugin upgradePlatformPlugin) {
    if (log.isDebugEnabled()) {
      log.debug("Add Platform UpgradePlugin: name = " + upgradePlatformPlugin.getName());
    }
    upgradePlugins.add(upgradePlatformPlugin);
  }

  /**
   * This method is called by eXo Kernel when starting the parent ExoContainer
   */
  @Override
  public void start() {
    if (log.isDebugEnabled()) {
      log.debug("start method begin");
    }

    if (workspaceName == null || workspaceName.equals("")) {
      log.info("Workspace wasn't specified, use default one.");
      try {
        workspaceName = repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName();
      } catch (RepositoryException exception) {
        log.error(exception);
      } catch (RepositoryConfigurationException exception) {
        log.error(exception);
      }
    }

    Session session = null;
    try {
      // Get a JCR Session
      session = getSession();

      // Read Platform's version declared in the JCR
      Node platformVersionDeclarationNode = readPlatformVersion(session);

      if (!oldVersion.equals(currentVersion)) {// The version of Platform server has changed
        log.info("New Platform version is detected: proceed upgrading ...");

        for (UpgradePlatformPlugin upgradePlatformPlugin : upgradePlugins) {
          upgradePlatformPlugin.processUpgrade(oldVersion, currentVersion);
        }

        // The platform has been upgraded successfully, so we have to change the platform version in the JCR
        storePlatformVersion(platformVersionDeclarationNode);

        log.info("Platform upgraded successfully.");
        /* End: Proceed upgrading Platform */
      }
    } catch (LoginException exception) {
      log.error("Platform upgrade error!");
      throw new RuntimeException(exception);
    } catch (NoSuchWorkspaceException exception) {
      log.error("Platform upgrade error!");
      throw new RuntimeException(exception);
    } catch (RepositoryException exception) {
      log.error("Platform upgrade error!");
      throw new RuntimeException(exception);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("start method end");
    }
  }

  /**
   * This method is called by eXo Kernel when stopping the parent ExoContainer
   */
  @Override
  public void stop() {}

  private void storePlatformVersion(Node platformVersionDeclarationNode) throws VersionException,
      UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException,
      PathNotFoundException, ValueFormatException, ConstraintViolationException, AccessDeniedException, ItemExistsException,
      NoSuchNodeTypeException {
    Version version = platformVersionDeclarationNode.checkin();
    platformVersionDeclarationNode.getSession().save();
    platformVersionDeclarationNode.getVersionHistory().addVersionLabel(version.getName(), oldVersion, false);
    platformVersionDeclarationNode.checkout();
    Node platformVersionDeclarationNodeContent = platformVersionDeclarationNode.getNode("jcr:content");
    platformVersionDeclarationNodeContent.setProperty("jcr:data", currentVersion);
    platformVersionDeclarationNodeContent.setProperty("jcr:lastModified", new Date().getTime());
    platformVersionDeclarationNode.getSession().save();
  }

  private Node readPlatformVersion(Session session) throws RepositoryException, PathNotFoundException, ItemExistsException,
      NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, ValueFormatException,
      AccessDeniedException, InvalidItemStateException {
    // get "Application Data" node
    Node applicationDataNode = null;
    if (session.getRootNode().hasNode(applicationDataRootNodePath)) {
      applicationDataNode = session.getRootNode().getNode(applicationDataRootNodePath);
    } else {
      if (log.isDebugEnabled()) {
        log.debug("'Application Data' doesn't exist, creating it ... ");
      }
      applicationDataNode = session.getRootNode().addNode(applicationDataRootNodePath, "nt:unstructured");
      applicationDataNode.addMixin("exo:hiddenable");
    }

    // This node's path is "collaboration:/exo;services/UpgradePlatformService/platformVersionDeclarationNode"
    Node platformVersionDeclarationNode = null;
    if (applicationDataNode.hasNode(UPGRADE_PLATFORM_SERVICE_NODE_NAME)) {
      platformVersionDeclarationNode = applicationDataNode.getNode(UPGRADE_PLATFORM_SERVICE_NODE_NAME + "/"
          + PLATFORM_VERSION_DECLARATION_NODE_NAME);
      // get the platform old version declaration, stored in a JCR property, this will be:
      // "collaboration:/exo;services/UpgradePlatformService/platformVersionDeclarationNode/jcr:content/jcr:data"
      oldVersion = ((Property) session.getItem(platformVersionDeclarationNode.getPath() + "/jcr:content/jcr:data")).getString();
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Platform server first run: setup platform Version Declaration Node");
      }
      // The database is clean, so this is the first start of the Platform server
      oldVersion = FIRST_VERSION_LABEL;
      Node UpgradePlatformServiceNode = applicationDataNode.addNode(UPGRADE_PLATFORM_SERVICE_NODE_NAME, "nt:unstructured");
      platformVersionDeclarationNode = UpgradePlatformServiceNode.addNode(PLATFORM_VERSION_DECLARATION_NODE_NAME, "nt:file");
      Node platformVersionDeclarationNodeContent = platformVersionDeclarationNode.addNode("jcr:content", "nt:resource");
      platformVersionDeclarationNodeContent.setProperty("jcr:encoding", "UTF-8");
      platformVersionDeclarationNodeContent.setProperty("jcr:mimeType", "text/plain");
      platformVersionDeclarationNodeContent.setProperty("jcr:data", oldVersion);
      platformVersionDeclarationNodeContent.setProperty("jcr:lastModified", new Date().getTime());
      if (!platformVersionDeclarationNode.isNodeType(MIX_VERSIONABLE)) {
        platformVersionDeclarationNode.addMixin(MIX_VERSIONABLE);
      }
      session.save();
      session.refresh(true);
    }
    /* End: store/read Platform version from the JCR */
    return platformVersionDeclarationNode;
  }

  private Session getSession() throws RepositoryException, LoginException, NoSuchWorkspaceException {
    Session session;
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    session = sessionProvider.getSession(workspaceName, repository);
    return session;
  }

}