// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TermsAndConditionsServiceImpl.java

package org.exoplatform.welcomescreens.service.impl;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.welcomescreens.service.TermsAndConditionsService;

/**
 * This service is used to manage Terms and conditions
 * 
 * @author Clement
 */
public class TermsAndConditionsServiceImpl implements TermsAndConditionsService {

  private static final Log     LOG                       = ExoLogger.getLogger(TermsAndConditionsServiceImpl.class);

  private static final String  TERMS_AND_CONDITIONS      = "TermsAndConditions";

  private static final Context CONTEXT                   = Context.GLOBAL.id(TERMS_AND_CONDITIONS);

  private static final Scope   SCOPE                     = Scope.APPLICATION.id(TERMS_AND_CONDITIONS);

  private PortalContainer      container;

  private SettingService       settingService;

  private boolean              hasTermsAndConditionsNode = false;

  public TermsAndConditionsServiceImpl(PortalContainer container, SettingService settingService) {
    this.container = container;
    this.settingService = settingService;
  }

  /*
   * ======================================================================= API
   * public methods
   * ======================================================================
   */
  public boolean isTermsAndConditionsChecked() {
    boolean isChecked = false;
    if (hasTermsAndConditions()) {
      isChecked = true;
    }
    return isChecked;
  }

  public void checkTermsAndConditions() {
    if (!hasTermsAndConditions()) {
      createTermsAndConditions();
    } else {
      LOG.debug("Terms and conditions: yet checked");
    }
  }

  /*
   * ======================================================================= API
   * private methods
   * ======================================================================
   */
  private void createTermsAndConditions() {
    RequestLifeCycle.begin(container);
    try {
      settingService.set(CONTEXT, SCOPE, TERMS_AND_CONDITIONS, SettingValue.create(true));
    } catch (Exception e) {
      LOG.error("Terms and conditions: cannot save information", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private boolean hasTermsAndConditions() {
    RequestLifeCycle.begin(container);
    try {
      // --- Initial hasTermsAndConditionsNode is false we need to get flag from
      // store
      if (hasTermsAndConditionsNode) {
        return true;
      } else {
        try {
          // --- Get The session Provider
          SettingValue<?> value = settingService.get(CONTEXT, SCOPE, TERMS_AND_CONDITIONS);
          hasTermsAndConditionsNode = value != null && value.getValue() != null;
        } catch (Exception E) {
          LOG.error("Terms and conditions: connot get information from store", E);
          hasTermsAndConditionsNode = false;
        } finally {
          RequestLifeCycle.end();
        }
        return hasTermsAndConditionsNode;
      }

    } catch (Exception e) {
      LOG.error("Terms and conditions: cannot check node", e);
    }
    return hasTermsAndConditionsNode;

  }
}
