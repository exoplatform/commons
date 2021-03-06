package org.exoplatform.commons.search.integration;

import org.elasticsearch.Version;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;

/**
 * Node allowing to declare a list of plugins
 */
public class EmbeddedNode extends Node {

  public static final String ES_VERSION = "5.6.3";
  private Collection<Class<? extends Plugin>> plugins;

  public EmbeddedNode(Environment environment, Version version, Collection<Class<? extends Plugin>> classpathPlugins) {
    super(environment, classpathPlugins);
    this.plugins = classpathPlugins;
  }

  public Collection<Class<? extends Plugin>> getPlugins() {
    return plugins;
  }
}

