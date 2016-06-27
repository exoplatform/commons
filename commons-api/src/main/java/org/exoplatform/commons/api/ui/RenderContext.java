package org.exoplatform.commons.api.ui;

import java.util.ResourceBundle;

import org.gatein.pc.api.ActionURL;

public class RenderContext extends BaseContext {

    private String actionUrl;

    private ResourceBundle rsBundle;

    public RenderContext(String pluginType) {
        super(pluginType);
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public ResourceBundle getRsBundle() {
        return rsBundle;
    }

    public void setRsBundle(ResourceBundle rsBundle) {
        this.rsBundle = rsBundle;
    }

    public String resolve(String key) {
        if (rsBundle != null) {
            return rsBundle.getString(key);
        } else {
            return key;
        }
    }
}
