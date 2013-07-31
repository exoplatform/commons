package org.exoplatform.commons.api.notification.template;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;

public interface ElementVisitor {
	ElementVisitor visit(Element element);
	
	/**
	 * Gets the content of template after generates.
	 * @return
	 */
	String out();
	
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
