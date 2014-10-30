package org.exoplatform.webui.form;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
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

  private boolean isIgnoreParserHTML = false;

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

  public UIFormRichtextInput setIsPasteAsPlainText(boolean isPasteAsPlainText) {
    this.isPasteAsPlainText = isPasteAsPlainText;
    return this;
  }

  public boolean getIsPasteAsPlainText() {
    return this.isPasteAsPlainText;
  }

  public boolean isIgnoreParserHTML() {
    return isIgnoreParserHTML;
  }

  public UIFormRichtextInput setIgnoreParserHTML(boolean isIgnoreParserHTML) {
    this.isIgnoreParserHTML = isIgnoreParserHTML;
    return this;
  }

  public void setCss(String css) {
    this.css = css;
  }

  public String getCss() {
    return css;
  }

  private static String encodeURLComponent(String s) {
    String result = null;
    try {
      result = URLEncoder.encode(s, "UTF-8")
                         .replaceAll("\\+", "%20")
                         .replaceAll("\\%21", "!")
                         .replaceAll("\\%28", "(")
                         .replaceAll("\\%29", ")")
                         .replaceAll("\\%7E", "~");
    } catch (UnsupportedEncodingException e) {
      result = s;
    }
    return result;
  }

  private String buildEditorLayout() throws Exception {
    if (toolbar == null) toolbar = BASIC_TOOLBAR;
    if (width == null) width = "98%";
    if (height == null) height = "'200px'";
    if (enterMode == null) enterMode = "1";
    if (css == null) css = "\"/CommonsResources/ckeditor/contents.css\"";
    if (value_ == null) value_ = "";

    StringBuilder builder = new StringBuilder();
    builder.append("<div class=\"clearfix\">");
    builder.append("  <span style=\"float:left; width:").append(width).append(";\">");
    //
    builder.append("  <textarea id=\"").append(name).append("\" name=\"").append(name).append("\">")
          .append(value_).append("</textarea>\n");

    builder.append("<script type=\"text/javascript\">\n");
    //fix issue INTEG-320
    if (isIgnoreParserHTML() && StringUtils.isNotEmpty(value_)) {
      String value = encodeURLComponent(value_);
      builder.append(" var textare = document.getElementById('").append(name).append("'); ")
             .append(" if(textare) {")
             .append("   var isFirefox = typeof InstallTrigger !== 'undefined';")
             .append("   var value = decodeURIComponent('").append(value).append("');")
             .append("   if(isFirefox) { textare.value = value; } else { textare.innerText = value;}")
             .append(" }");
    }
    builder.append("    require(['/CommonsResources/ckeditor/ckeditor.js'], function() {")
           .append("  //<![CDATA[\n")
           .append("    var instance = CKEDITOR.instances['").append(name).append("'];")
           .append("    if (instance) { CKEDITOR.remove(instance); instance = null;}\n");
    
    builder.append("    CKEDITOR.replace('").append(name).append("', {toolbar:'").append(toolbar).append("', height:")
           .append(height).append(", contentsCss:").append(css).append(", enterMode:").append(enterMode)
           .append((isPasteAsPlainText) ? ", forcePasteAsPlainText: true" : "").append("});\n");

    builder.append("    instance = CKEDITOR.instances['" + name + "'];")
           .append("    instance.on( 'change', function(e) { document.getElementById('").append(name).append("').value = instance.getData(); });\n")
           //workaround, fix IE case: can not focus to editor
           .append("  //]]>\n")
           .append("});")
           .append("if(eXo.core.Browser.ie==9 || eXo.core.Browser.ie==10){")
           .append(" var textare = document.getElementById('").append(name).append("'); ")
           .append(" var form = textare;")
           .append(" while (form && (form.nodeName.toLowerCase() != 'form')) { form = form.parentNode;}")
           .append(" form.onmouseover=function(){")
           .append("  this.onmouseover='';")
           .append("  var textare = document.getElementById('").append(name).append("'); ")
           .append("  textare.style.display='block';")
           .append("  textare.style.visibility='visible';")
           .append("  textare.focus();")           
           .append("  textare.style.display='none';")
           .append(" }")
           .append("}")           

           .append("</script>\n");

    builder.append("  </span>");

    if (isMandatory()) {
      builder.append("  <span style=\"float:left\"> &nbsp;*</span>");
    }
    builder.append("</div>");
    //
    return builder.toString();
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    //
    context.getWriter().write(buildEditorLayout());
  }

  public void decode(Object input, WebuiRequestContext context) {
    value_ = (String) input;
    if (value_ != null && value_.length() == 0) {
      value_ = null;
    }
  }

}