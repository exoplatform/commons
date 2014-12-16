package org.exoplatform.commons.notification;

import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.commons.notification.mock.AddNodeEventListener;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public abstract class BaseNotificationTestCase extends BaseCommonsTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    addNodeEventListener();
  }
  
  @Override
  protected void tearDown() throws Exception {
    session.logout();
  }

  protected void addNodeEventListener() throws Exception {
    try {
      ObservationManager observation = session.getWorkspace().getObservationManager();
      AddNodeEventListener addNodeListener = new AddNodeEventListener();
      addNodeListener.setSession(session);
      observation.addEventListener(addNodeListener, Event.NODE_ADDED, "/", true, null, new String[] {"nt:base"}, false);
    } catch (Exception e) {}
  }
}
