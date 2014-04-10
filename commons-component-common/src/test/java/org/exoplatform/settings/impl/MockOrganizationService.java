package org.exoplatform.settings.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;
import org.exoplatform.settings.listeners.impl.CommonsUserSettingEventListenerImpl;

public class MockOrganizationService extends DummyOrganizationService {
  
  static List<UserEventListener> listeners = new ArrayList<UserEventListener>();

  public MockOrganizationService() {
    super();
    this.userDAO_ = new MockUserHandlerImpl();
  }
  
  
  public static class MockUserHandlerImpl extends UserHandlerImpl {
    @Override
    public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception {
      
      User user = findUserByName(userName, UserStatus.ANY);
      ((UserImpl) user).setEnabled(enabled);
      listeners.add(new CommonsUserSettingEventListenerImpl());
      for (UserEventListener listener : listeners)
      {
         listener.postSetEnabled(user);
      }
      //
      return user;
    }
  }
  
}
