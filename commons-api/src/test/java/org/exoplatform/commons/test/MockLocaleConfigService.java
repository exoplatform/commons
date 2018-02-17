package org.exoplatform.commons.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.impl.LocaleConfigImpl;


/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 16/02/18 17:44
 */

public class MockLocaleConfigService implements LocaleConfigService {

  @Override
  public LocaleConfig getDefaultLocaleConfig() {
    return new LocaleConfigImpl();
  }

  @Override
  public LocaleConfig getLocaleConfig(String lang) {
    return new LocaleConfigImpl();
  }

  @Override
  public Collection<LocaleConfig> getLocalConfigs() {
    Collection<LocaleConfig> localConfigs = new ArrayList<>();
    Locale [] locales = {Locale.FRENCH,Locale.GERMAN, Locale.ENGLISH,Locale.FRANCE};
    for(Locale locale : locales) {
      LocaleConfig config = new LocaleConfigImpl();
      config.setLocale(locale);
      localConfigs.add(config);
    }
    return localConfigs;
  }
}
