package org.exoplatform.commons.file.resource;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.util.HashMap;
import java.util.Map;

/**
 * This Component Plugin allows you to dynamically define a resource provider.
 */
public class ResourceProviderPlugin extends BaseComponentPlugin {
    private Map<String, String> resourceProviderData = new HashMap<>();
    private static final String STORAGE_TYPE = "storageType";
    private static final String CLASS_NAME = "class";

    public ResourceProviderPlugin(InitParams initParams) {
        if (initParams != null) {
            ValueParam typeParam  = initParams.getValueParam(STORAGE_TYPE);
            ValueParam classParam  = initParams.getValueParam(CLASS_NAME);
            if (typeParam != null && classParam != null && !typeParam.getValue().isEmpty() && !classParam.getValue().isEmpty())
                resourceProviderData.put(typeParam.getValue(), classParam.getValue());
        }
    }

    public Map<String, String> getResourceProviderData() {
        return resourceProviderData;
    }

    public void setResourceProviderData(Map<String, String> resourceProviderData) {
        this.resourceProviderData = resourceProviderData;
    }
}
