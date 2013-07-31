package org.exoplatform.commons.api.notification.template;

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
}
