/**
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.webui.container;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.addons.AddOnService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.container.UIContainerForm;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(events = { @EventConfig(listeners = UIAddOnContainer.EditContainerActionListener.class),
        @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIContainer.deleteContainer") })
public class UIAddOnContainer extends UIContainer {

    public static final String ADDON_CONTAINER = "addonContainer";

    private boolean initialized = false;

    @Override
    public List<UIComponent> getChildren() {
        if (!initialized) {
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            AddOnService service = (AddOnService)container.getComponentInstanceOfType(AddOnService.class);

            List<Application<?>> apps = service.getApplications(this.getName());
            Container model = new Container();
            model.setChildren(new ArrayList<ModelObject>(apps));
            try {
                UIContainer tmp = new UIContainer();
                PortalDataMapper.toUIContainer(tmp, model);
                for (UIComponent comp : tmp.getChildren()) {
                    comp.setParent(this);
                }
                this.setChildren(tmp.getChildren());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            initialized = true;
        }
        return super.getChildren();
    }

    @Override
    public ModelObject buildModelObject() {
        Container model = new Container(getStorageId());
        model.setId(getId());
        model.setName(getName());
        model.setTitle(getTitle());
        model.setIcon(getIcon());
        model.setDescription(getDescription());
        model.setHeight(getHeight());
        model.setWidth(getWidth());
        model.setTemplate(getTemplate());
        model.setFactoryId(getFactoryId());
        model.setAccessPermissions(getAccessPermissions());
        //Don't build children, we don't save them to database
        return model;
    }
    
    public static class EditContainerActionListener extends EventListener<UIContainer> {
        public void execute(Event<UIContainer> event) throws Exception {
            UIContainer uiContainer = event.getSource();
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            UIContainerForm containerForm = uiMaskWS.createUIComponent(UIAddOnContainerForm.class, "UIContainerForm", "UIContainerForm");
            containerForm.setValues(uiContainer);
            uiMaskWS.setUIComponent(containerForm);
            uiMaskWS.setShow(true);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
        }
    }
}
