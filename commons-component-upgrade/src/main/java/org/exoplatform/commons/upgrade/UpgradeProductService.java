package org.exoplatform.commons.upgrade;

import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradeProductService implements Startable {

  private static final Log LOG = ExoLogger.getLogger(UpgradeProductService.class);
  private static final String PLUGINS_ORDER = "commons.upgrade.plugins.order";
  private static final String PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";
  private static final String PRODUCT_VERSION_ZERO = "0";

  private Set<UpgradeProductPlugin> upgradePlugins = new TreeSet<UpgradeProductPlugin>();
  private Set<UpgradeProductPlugin> allUpgradePlugins= new TreeSet<UpgradeProductPlugin>();
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
      LOG.warn("init param '" + PROCEED_UPGRADE_FIRST_RUN_KEY + "' isn't set, use default value (" + proceedUpgradeFirstRun
          + "). Don't proceed upgrade when this service will run for the first time.");
    } else {
      proceedUpgradeFirstRun = Boolean.parseBoolean(initParams.getValueParam(PROCEED_UPGRADE_FIRST_RUN_KEY).getValue());
    }
  }

  /**
   * Method called by eXo Kernel to inject upgrade plugins
   * 
   * @param upgradeProductPlugin
   */
  public void addUpgradePlugin(UpgradeProductPlugin upgradeProductPlugin) {
    LOG.info("Add Product UpgradePlugin: name = " + upgradeProductPlugin.getName());
    if (upgradePlugins.contains(upgradeProductPlugin)) {
      LOG.warn(upgradeProductPlugin.getName() + " upgrade plugin is duplicated. One of the duplicated plugins will be ignored!");
    }
    // add only enabled plugins
    if (upgradeProductPlugin.isEnabled()) {
      upgradePlugins.add(upgradeProductPlugin);
      allUpgradePlugins.add(upgradeProductPlugin);
    } else {
      LOG.info("UpgradePlugin: name = '" + upgradeProductPlugin.getName() + "' will be ignored, because it is not enabled.");
    }
  }

  /**
   * This method is called by eXo Kernel when starting the parent
   * ExoContainer
   */
  public void start() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("start method begin");
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
        LOG.info("New version has been detected: proceed upgrading from " + previousVersion + " to " + currentVersion);
        // Gets the execution order property:
        // "commons.upgrade.plugins.order".
        String pluginsOrder = PropertyManager.getProperty(PLUGINS_ORDER);
        // If the property does not exist, rely on the plugin execution
        // order: the order of the upgradeProductPlugin in upgradePlugins.
        if (pluginsOrder == null) {
          for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
            doUpgrade(upgradeProductPlugin, upgradeProductPlugin.getPluginExecutionOrder());
            LOG.info("Upgrade " + upgradeProductPlugin.getName() + " completed.");
          }
        } else {
          // If the property contains names of upgradeProductPlugins,
          // execute them with their names' appearance order.
          String upgradePluginNames[] = pluginsOrder.split(",");
          for (int i = 0; i < upgradePluginNames.length; i++) {
            if (upgradePlugins.size() > 0) {
              Iterator<UpgradeProductPlugin> iterator= upgradePlugins.iterator();
              while(iterator.hasNext()) {
                UpgradeProductPlugin upgradeProductPlugin= iterator.next();
                if (upgradeProductPlugin.getName().equals(upgradePluginNames[i])) {
                  doUpgrade(upgradeProductPlugin, i);
                  iterator.remove();
                  break;
                }
              }
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
        LOG.info("Version upgrade completed.");
      }
    } catch (MissingProductInformationException missingProductInformationException) {
      LOG.error("Can't proceed to the upgrade", missingProductInformationException);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("start method end");
    }
  }

  private void doUpgrade(UpgradeProductPlugin upgradeProductPlugin, int order) {
    try {
      String currentProductPluginVersion = productInformations.getVersion(upgradeProductPlugin.getProductGroupId());
      
      String previousProductPluginVersion ="";
      try{
        previousProductPluginVersion= productInformations.getPreviousVersion(upgradeProductPlugin.getOldProductGroupId());
      }
      catch (Exception e) {
         previousProductPluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getProductGroupId());
      }

      //
      if (upgradeProductPlugin.shouldProceedToUpgrade(currentProductPluginVersion, previousProductPluginVersion)) {
        LOG.info("Proceed upgrade plugin: name = " + upgradeProductPlugin.getName() + " from version "
            + previousProductPluginVersion + " to " + currentProductPluginVersion + " with execution order = " + order);
        upgradeProductPlugin.processUpgrade(previousProductPluginVersion, currentProductPluginVersion);
      } else {
        LOG.info("'" + upgradeProductPlugin.getName()
            + "' upgrade plugin execution will be ignored because shouldProceedToUpgrade = false");
      }
    } catch (MissingProductInformationException exception) {
      LOG.error("The plugin " + upgradeProductPlugin.getName() + " generated an error.", exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {}
  
  /**
   * Re-import all upgrade-plugins for service
   */
  public void resetService()
  {
    //Reset product information
    productInformations.start();
    
    //Reload list Upgrade-Plugins
    upgradePlugins.clear();
    Iterator<UpgradeProductPlugin> iterator= allUpgradePlugins.iterator();
    while(iterator.hasNext())
    {
      UpgradeProductPlugin upgradeProductPlugin= iterator.next();
      upgradePlugins.add(upgradeProductPlugin);
    }
  }

}
