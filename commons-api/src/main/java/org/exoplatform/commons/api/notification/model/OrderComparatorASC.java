package org.exoplatform.commons.api.notification.model;

import java.util.Comparator;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;

public class OrderComparatorASC implements Comparator<Object> {
  @Override
  public int compare(Object o1, Object o2) {
    if (o1 instanceof GroupProvider && o2 instanceof GroupProvider) {
      Integer order1 = ((GroupProvider) o1).getOrder();
      Integer order2 = ((GroupProvider) o2).getOrder();
      return order1.compareTo(order2);
    }
    if (o1 instanceof PluginConfig && o2 instanceof PluginConfig) {
      Integer order1 = Integer.parseInt(((PluginConfig) o1).getOrder());
      Integer order2 = Integer.parseInt(((PluginConfig) o2).getOrder());
      return order1.compareTo(order2);
    }
    if (o1 instanceof PluginInfo && o2 instanceof PluginInfo) {
      Integer order1 = ((PluginInfo) o1).getOrder();
      Integer order2 = ((PluginInfo) o2).getOrder();
      return order1.compareTo(order2);
    }
    return 0;
  }
}
