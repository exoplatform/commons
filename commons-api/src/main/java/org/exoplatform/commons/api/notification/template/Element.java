package org.exoplatform.commons.api.notification.template;

import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;

public interface Element {
  /**
   * Gets the language what belongs to the template
   * @return
   */
  String getLanguage();
  
  /**
   * Accept the visitor to visit the element 
   * @param visitor
   * @return
   */
  ElementVisitor accept(ElementVisitor visitor);
  
  /**
   * Gets the template of specified element
   * @return
   */
  String getTemplate();

  /**
   * Assigns the language to 
   * @param language
   * @return
   */
  Element language(String language);
  
  /**
   * Assigns the template to the element
   * @param template
   * @return
   */
  Element template(String template);
  
  /**
   * Gets template configure for the element
   * uses it in the case when we need to get the groovy template.
   * @return
   */
  TemplateConfig getTemplateConfig();
  
  /**
   * Sets the template configure for the element 
   * @param templateConfig
   * @return
   */
  Element config(TemplateConfig templateConfig);

  /**
   * Set the value isNewLine for the case digest
   * @param needNewLine
   * @return
   */
  Element addNewLine(boolean needNewLine);

  /**
   * Get the value of isNewLine
   * @return
   */
  boolean isNewLine();
}
