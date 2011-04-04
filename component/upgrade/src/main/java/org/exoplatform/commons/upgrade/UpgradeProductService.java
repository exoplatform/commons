package org.exoplatform.commons.upgrade;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradeProductService implements Startable {

  private static final Log log = ExoLogger.getLogger(UpgradeProductService.class);

  private Set<UpgradeProductPlugin> upgradePlugins = new HashSet<UpgradeProductPlugin>();
  private ProductInformations productInformations = null;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param productInformations
   */
  public UpgradeProductService(ProductInformations productInformations, InitParams initParams) {
    this.productInformations = productInformations;
  }

  /**
   * Method called by eXo Kernel to inject upgrade plugins
   * 
   * @param upgradeProductPlugin
   */
  public void addUpgradePlugin(UpgradeProductPlugin upgradeProductPlugin) {
    if (log.isDebugEnabled()) {
      log.debug("Add Product UpgradePlugin: name = " + upgradeProductPlugin.getName());
    }
    if (upgradePlugins.contains(upgradeProductPlugin.getName())) {
      log.warn(upgradeProductPlugin.getName() + " upgrade plugin is duplicated. One of duplicated plugins will be ignore!");
    }
    upgradePlugins.add(upgradeProductPlugin);
  }

  /**
   * This method is called by eXo Kernel when starting the parent ExoContainer
   */
  public void start() {
    if (log.isDebugEnabled()) {
      log.debug("start method begin");
    }

    // Get a JCR Session
    String currentVersion;
    try {
      currentVersion = productInformations.getVersion();
      String previousVersion = productInformations.getPreviousVersion();
      if (!previousVersion.equals(currentVersion)) {// The version of Product server has changed
        log.info("New version has been detected: proceed upgrading from " + previousVersion + " to " + currentVersion);
        for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
          try {
            String currentProductPluginVersion = productInformations.getVersion(upgradeProductPlugin.getProductGroupId());
            String previousProductPluginVersion = productInformations
                .getPreviousVersion(upgradeProductPlugin.getProductGroupId());

            if (upgradeProductPlugin.shouldProceedToUpgrade(currentProductPluginVersion, previousProductPluginVersion)) {
              log.info("Proceed upgrade plugin: name = " + upgradeProductPlugin.getName() + " from version "
                  + previousProductPluginVersion + " to " + currentProductPluginVersion);
              upgradeProductPlugin.processUpgrade(previousProductPluginVersion, currentProductPluginVersion);
            }
          } catch (Exception exception) {
            log.error("The plugin " + upgradeProductPlugin.getName() + " generated an error.", exception);
            continue;
          }
          log.info("Upgrade " + upgradeProductPlugin.getName() + " completed.");
        }
        // The product has been upgraded, change the product version in the JCR
        productInformations.storeProductsInformationsInJCR();

        log.info("Version upgrade completed.");
      }
    } catch (MissingProductInformationException missingProductInformationException) {
      log.error("Can't proceed to the upgrade", missingProductInformationException);
    }
    if (log.isDebugEnabled()) {
      log.debug("start method end");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {}

}
