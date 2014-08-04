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

import java.util.List;

import org.exoplatform.portal.webui.container.UIContainerForm;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

@ComponentConfigs({
    @ComponentConfig(id = "UIContainerForm", lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
            @EventConfig(listeners = UIContainerForm.SaveActionListener.class),
            @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE) }),
    @ComponentConfig(id = "UIContainerPermission", type = UIFormInputSet.class, lifecycle = UIContainerLifecycle.class) })
public class UIAddOnContainerForm extends UIContainerForm {

    public UIAddOnContainerForm() throws Exception {
        super();
        UIFormInputSet infoInputSet = this.findComponentById("ContainerSetting");
        UIFormInputBase<String> input = new UIFormStringInput("name", "name", null);
        input.addValidator(MandatoryValidator.class);
        input.addValidator(StringLengthValidator.class, 3, 30);
        input.addValidator(NameValidator.class);
        input.setParent(infoInputSet);

        List<UIComponent> children = infoInputSet.getChildren();
        children.add(1, input);
        infoInputSet.setChildren(children);
    }

}