package org.exoplatform.commons.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;

/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 16/02/18 16:33
 */
public class MockOrganizationService extends DummyOrganizationService {

  static List<UserEventListener> listeners = new ArrayList<UserEventListener>();

  public MockOrganizationService() {
    super();
    this.userDAO_ = new MockUserHandlerImpl();
    this.userProfileDAO_ = new MockUserProfileHandler();
  }

  public void setMockUserHandlerImpl(UserHandlerImpl handlerImpl) {
    this.userDAO_ = handlerImpl;
  }

  public static class MockUserHandlerImpl extends UserHandlerImpl {

  }

  public class MockUserProfileHandler extends DummyUserProfileHandler {

    private Map<String,UserProfile> profiles = new HashMap<>();

    @Override
    public void saveUserProfile(UserProfile userProfile, boolean b) throws Exception {
      profiles.put(userProfile.getUserName(),userProfile);
    }

    @Override
    public UserProfile findUserProfileByName(String s) throws Exception {
      return profiles.get(s);
    }

  }

}

