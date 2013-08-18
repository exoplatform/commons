package org.exoplatform.commons.api.notification.template;

import java.io.Writer;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;

public interface ElementVisitor {
  /**
   * To visit the element and generate
   * @param element
   * @return
   */
	ElementVisitor visit(Element element);
	
	/**
	 * Gets the content of template after generates.
	 * @return
	 */
	String out();
	
	/**
   * Gets the writer.
   * @return
   */
  Writer getWriter();
	
	/**
	 * Gets the template context
	 * @return
	 */
	TemplateContext getTemplateContext();
	
	/**
	 * Attaches the Template Context for Template generate.
	 * @param ctx The Template Context for generating
	 * @return
	 */
	ElementVisitor with(TemplateContext ctx);
}
