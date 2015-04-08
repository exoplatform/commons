package org.exoplatform.commons.notification.mock;

import java.io.Writer;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.channel.template.DigestMailTemplateProvider;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;

@TemplateConfigs(
   templates = {
       @TemplateConfig(pluginId = PluginTest.ID, template = "classpath:/groovy/notification/template/TestPlugin.gtmpl"),
       @TemplateConfig(pluginId = DigestDailyPlugin.ID, template = "classpath:/groovy/notification/template/provider1.gtmpl"),
       @TemplateConfig(pluginId = DigestWeeklyPlugin.ID, template = "classpath:/groovy/notification/template/provider1.gtmpl")
   }
 )
public class MockDigestMailTemplateProvider extends DigestMailTemplateProvider {

  public MockDigestMailTemplateProvider(InitParams initParams) {
    super(initParams);
    templateBuilders.put(new PluginKey(PluginTest.ID), testBuilder);
  }
  AbstractTemplateBuilder testBuilder = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      
      
      templateContext.put("USER", notification.getValueOwnerParameter("USER"));
      templateContext.put("SUBJECT", "Test plugin notification");
      String subject = TemplateUtils.processSubject(templateContext);

      String value = notification.getValueOwnerParameter("TEST_VALUE");
      templateContext.put("VALUE", value);
      StringBuilder childContent = new StringBuilder();
      
      PluginContainer pluginContainer = CommonsUtils.getService(PluginContainer.class);
      List<PluginKey> childKeys = pluginContainer.getChildPluginKeys(new PluginKey(PluginTest.ID));
      for (PluginKey notificationKey : childKeys) {
        BaseNotificationPlugin child = pluginContainer.getPlugin(notificationKey);
        childContent.append("<br>").append(((AbstractNotificationChildPlugin) child).makeContent(ctx));
      }
      templateContext.put("CHILD_CONTENT", childContent.toString());
      
      return new MessageInfo().subject(subject).body(TemplateUtils.processGroovy(templateContext)).end();
    }
    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };
}
