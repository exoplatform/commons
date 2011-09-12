package org.exoplatform.commons.upgrade;

import java.util.Set;
import java.util.TreeSet;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradeProductService implements Startable {

  private static final Log log = ExoLogger.getLogger(UpgradeProductService.class);
  private static final String PLUGINS_ORDER = "commons.upgrade.plugins.order";
  private static final String PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";
  private static final String PRODUCT_VERSION_ZERO = "0";

  private Set<UpgradeProductPlugin> upgradePlugins = new TreeSet<UpgradeProductPlugin>();
  private ProductInformations productInformations = null;
  private boolean proceedUpgradeFirstRun = false;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param productInformations
   */
  public UpgradeProductService(ProductInformations productInformations, InitParams initParams) {
    this.productInformations = productInformations;
    if (!initParams.containsKey(PROCEED_UPGRADE_FIRST_RUN_KEY)) {
      log.warn("init param '" + PROCEED_UPGRADE_FIRST_RUN_KEY + "' isn't set, use default value (" + proceedUpgradeFirstRun
          + "). Don't proceed upgrade when this service will run for the first time.");
    } else {
      proceedUpgradeFirstRun = Boolean.getBoolean(initParams.getValueParam(PROCEED_UPGRADE_FIRST_RUN_KEY).getValue());
    }
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
    if (upgradePlugins.contains(upgradeProductPlugin)) {
      log.warn(upgradeProductPlugin.getName() + " upgrade plugin is duplicated. One of the duplicated plugins will be ignored!");
    }
    // add only enabled plugins
    if (upgradeProductPlugin.isEnabled()) {
      upgradePlugins.add(upgradeProductPlugin);
    }
  }

  /**
   * This method is called by eXo Kernel when starting the parent
   * ExoContainer
   */
  public void start() {
    if (log.isDebugEnabled()) {
      log.debug("start method begin");
    }

    // Set previous version declaration to Zero,
    // this will force the upgrade execution on first run
    if (proceedUpgradeFirstRun) {
      productInformations.setPreviousVersionsIfFirstRun(PRODUCT_VERSION_ZERO);
    }

    // Get a JCR Session
    String currentVersion;
    try {
      currentVersion = productInformations.getVersion();
      String previousVersion = productInformations.getPreviousVersion();
      if (!previousVersion.equals(currentVersion)) {// The version of
                                                    // Product server has
                                                    // changed
        log.info("New version has been detected: proceed upgrading from " + previousVersion + " to " + currentVersion);
        // Gets the execution order property:
        // "commons.upgrade.plugins.order".
        String pluginsOrder = PropertyManager.getProperty(PLUGINS_ORDER);
        // If the property does not exist, rely on the plugin execution
        // order: the order of the upgradeProductPlugin in upgradePlugins.
        if (pluginsOrder == null) {
          for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
            doUpgrade(upgradeProductPlugin, upgradeProductPlugin.getPluginExecutionOrder());
            log.info("Upgrade " + upgradeProductPlugin.getName() + " completed.");
          }
        } else {
          // If the property contains names of upgradeProductPlugins,
          // execute them with their names' appearance order.
          String upgradePluginNames[] = pluginsOrder.split(",");
          for (int i = 0; i < upgradePluginNames.length; i++) {
            if (upgradePlugins.size() > 0) {
              for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
                if (upgradeProductPlugin.getName().equals(upgradePluginNames[i])) {
                  doUpgrade(upgradeProductPlugin, i);
                  upgradePlugins.remove(upgradeProductPlugin);
                  break;
                }
              }
            } else {
              // If the upgradePluginNames array contains more elements
              // than the upgradePlugins list, ignore these plugins.
              log.warn(upgradePluginNames[i] + " will be ignored!. \"" + PLUGINS_ORDER
                  + "\" property contains more elements than it should...");
            }
          }
          // If the upgradePluginNames array contains less elements than
          // the upgradePlugins list, execute these remaining plugins.
          if (upgradePlugins.size() > 0) {
            for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
              doUpgrade(upgradeProductPlugin, -1);
            }
          }
        }
        // The product has been upgraded, change the product version in the
        // JCR
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

  private void doUpgrade(UpgradeProductPlugin upgradeProductPlugin, int order) {
    try {
      String currentProductPluginVersion = productInformations.getVersion(upgradeProductPlugin.getProductGroupId());
      String previousProductPluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getProductGroupId());
      if (upgradeProductPlugin.shouldProceedToUpgrade(currentProductPluginVersion, previousProductPluginVersion)) {
        log.info("Proceed upgrade plugin: name = " + upgradeProductPlugin.getName() + " from version "
            + previousProductPluginVersion + " to " + currentProductPluginVersion + " with execution order = " + order);
        upgradeProductPlugin.processUpgrade(previousProductPluginVersion, currentProductPluginVersion);
      }
    } catch (Exception exception) {
      log.error("The plugin " + upgradeProductPlugin.getName() + " generated an error.", exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {}

}
