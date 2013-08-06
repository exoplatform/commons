package org.exoplatform.webui.form;


import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Ha Quang Tan
 *          tanhq@exoplatform.com
 * July 15, 2013
 */
public class UIFormRichtextInput extends UIFormInputBase<String> {

  public static final String FULL_TOOLBAR = "CompleteWCM";
  public static final String BASIC_TOOLBAR = "Basic";
  public static final String SUPER_BASIC_TOOLBAR = "SuperBasicWCM";
  public static final String INLINE_TOOLBAR = "InlineEdit";
  public static final String FORUM_TOOLBAR = "Forum";
  public static final String FAQ_TOOLBAR = "FAQ";
  
  public static final String ENTER_P = "1";
  public static final String ENTER_BR = "2";
  public static final String ENTER_DIV = "3";

  private String width;

  private String height;

  private String toolbar;
  
  private String enterMode;
  
  private String css;
  
  private boolean isPasteAsPlainText = false;

  public UIFormRichtextInput(String name, String bindingField, String value) {
    super(name, bindingField, String.class);
    this.value_ = value;
  }

  public UIFormRichtextInput(String name, String bindingField, String value, String enterMode) {
    super(name, bindingField, String.class);
    this.value_ = value;
    this.enterMode = enterMode;
  }
  

  public UIFormRichtextInput(String name, String bindingField, String value, String enterMode, String toolbar) {
    super(name, bindingField, String.class);
    this.value_ = value;
    this.enterMode = enterMode;
    this.toolbar = toolbar;
  }
  
  
  public UIFormRichtextInput(String name, String bindingField, String value, String enterMode, String toolbar, String css) {
    super(name, bindingField, String.class);
	this.value_ = value;
	this.enterMode = enterMode;
	this.toolbar = toolbar;
	this.css = css;
  }
  
  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getToolbar() {
    return toolbar;
  }
  
  public String getEnterMode() {
  	return enterMode;
  }

  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }
  
  public void setEnterMode(String enterMode) {
  	this.enterMode = enterMode;
  }
  
  public void setIsPasteAsPlainText(boolean isPasteAsPlainText) {
	  this.isPasteAsPlainText = isPasteAsPlainText;
  }
  
  public boolean getIsPasteAsPlainText() {
	  return this.isPasteAsPlainText;
  }
  
  public void setCss(String css) {
	  this.css = css;
  }
  
  public String getCss() {
	  return css;
  }

  public void processRender(WebuiRequestContext context) throws Exception {

    if (toolbar == null) toolbar = BASIC_TOOLBAR;
    if (width == null) width = "98%";
    if (height == null) height = "'200px'";
    if (enterMode == null) enterMode = "1";
    if(css == null) css = "'/CommonsResources/ckeditor/contents.css'";
     

    StringBuffer buffer = new StringBuffer();
    buffer.append("<div>");
    buffer.append("<span style='float:left; width:"+width+";'>");
    if (value_!=null) {
      buffer.append("<textarea id='" + name + "' name='" + name + "'>" + value_ + "</textarea>\n");
    }else {
      buffer.append("<textarea id='" + name + "' name='" + name + "'></textarea>\n");
    }
    
    buffer.append("<script type='text/javascript'>\n");
    buffer.append("    require(['/CommonsResources/ckeditor/ckeditor.js'], function() {");
    buffer.append("  //<![CDATA[\n");
    buffer.append("    var instance = CKEDITOR.instances['" + name + "']; if (instance) { CKEDITOR.remove(instance); instance = null;}\n");
    if(isPasteAsPlainText)
    	buffer.append("    CKEDITOR.replace('" + name + "', {toolbar:'" + toolbar + "', height:"
    		+ height + ", forcePasteAsPlainText: true, contentsCss:" + css + ", enterMode:" + enterMode + ", shiftEnterMode:" + enterMode + "});\n");
    else
    	buffer.append("    CKEDITOR.replace('" + name + "', {toolbar:'" + toolbar + "', height:"
        	+ height + ", contentsCss:" + css + ", enterMode:" + enterMode + ", shiftEnterMode:" + enterMode + "});\n");
    buffer.append("    instance = CKEDITOR.instances['" + name + "']; instance.on( 'change', function(e) { document.getElementById('"+name+"').value = instance.getData(); }); \n");
    buffer.append("       });");
    buffer.append("  //]]>\n");
    buffer.append("</script>\n");
    buffer.append("</span>");
    if (isMandatory()) {
      buffer.append("<span style='float:left'> &nbsp;*</span>");
    }
    
    buffer.append("</div>");    
    context.getWriter().write(buffer.toString());
  }

  public void decode(Object input, WebuiRequestContext context) {
    value_ = (String)input;
    if (value_ != null && value_.length() == 0)
       value_ = null;
  }

}

