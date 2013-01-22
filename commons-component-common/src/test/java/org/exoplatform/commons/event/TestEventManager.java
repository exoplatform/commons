/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 **************************************************************************/
package org.exoplatform.commons.event;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import junit.framework.Assert;

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.commons.api.event.data.File;
import org.exoplatform.commons.event.impl.EventType;
import org.exoplatform.commons.listener.impl.AbstractEventListener;
import org.exoplatform.commons.model.impl.AbstractFile;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.listener.Event;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 22, 2012
 * 9:40:58 AM  
 */

/**
 * Test for <code>EventManager</code>
 */
public class TestEventManager<S, D> extends BaseCommonsTestCase {
    
    private EventManager<DMSFile, Integer> dmsEventManager_;
    
   
    @SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();
		dmsEventManager_ = getService(EventManager.class);
    }
    
    /**
     * 1. Test for register event listeners.
     *  
     *  Input:
     *   - Add 2 listeners to listen for DMSFile. 
     *  Output:
     *   - Return 2 listeners which added to listen DMSFile.
     * 
     * 2. Test for remove all the registered listeners.
     * 
     *  Input:
     *   - 2 listeners registered for DMSFile
     *  Output:
     *   - 0 listener for DMSFile
     * 
     */
    public void testManageEventListener() {
        
        //Init listeners which used to listen the change from DMSFile
        TestDMSFileListener dmsFileListener = new TestDMSFileListener();
        TestDMSFileListener dmsFileListener1 = new TestDMSFileListener();
        
        //Register event DMS listeners.
        dmsEventManager_.addEventListener("TestDMSFileListener", dmsFileListener);
        dmsEventManager_.addEventListener("TestDMSFileListener", dmsFileListener1);
        
        //Test to see the listener registered or not
        Assert.assertEquals(2, dmsEventManager_.getEventListeners("TestDMSFileListener").size());
        
        //Remove all registered listeners
        dmsEventManager_.removeEventListener("TestDMSFileListener", dmsFileListener);
        dmsEventManager_.removeEventListener("TestDMSFileListener", dmsFileListener1);
        
        //Test to make sure all listeners have been removed well.
        Assert.assertEquals(0, dmsEventManager_.getEventListeners("TestDMSFileListener").size());
    }
    
    /**
     * Broadcast for DMSFile
     * Input:
     *  - A listener registered to listen for DMSFile object
     * Output:
     *  - Broadcast event executed when file created
     *  - Broadcast event executed when file updated
     *  - Broadcast event executed when file deleted
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public void testBroadcastDMSEvent() throws PathNotFoundException, RepositoryException {
        
        //Init a listener to listen for DMSFile
        TestDMSFileListener dmsFileListener = new TestDMSFileListener();
        
        //Register listener to the DMS Event Manager
        dmsEventManager_.addEventListener("DMSFile", dmsFileListener);
        
        //Create a file and init an instance of DMSFile object
        Node testNode = createFile("testBroadcast");
        DMSFile dmsFile = new DMSFile(testNode.getSession().getWorkspace().getName(), testNode.getPath(), true);
       
        //Broadcast when a file created
        broadcastEvent(dmsFile, EventType.CREATED);
        Assert.assertTrue(dmsFileListener.fileCreated);
        
        //Broadcast when a file updated
        String strData = "File content updated";        
        testNode.getNode("jcr:content").getProperty("jcr:data").setValue(new ByteArrayInputStream(strData.getBytes()));
        testNode.save();
        broadcastEvent(dmsFile, EventType.UPDATE);
        Assert.assertTrue(dmsFileListener.fileUpdated);
        
        //Broadcast when a file removed
        testNode.remove();
        broadcastEvent(dmsFile, EventType.REMOVE);
        Assert.assertTrue(dmsFileListener.fileRemoved);
        session.save();
        
        //Remove the listener after broadcast all done
        dmsEventManager_.removeEventListener("DMSFile", dmsFileListener);
    }
    
    private void broadcastEvent(DMSFile dmsFile, int eventType) {
        dmsEventManager_.broadcastEvent(new Event<DMSFile, Integer>(dmsFile.getObjectType(), dmsFile, eventType));
    }
    
    private Node createFile(String name) {
        try {
            Node rootNode = session.getRootNode();
            Node fileNode = rootNode.addNode(name, "nt:file");
            Node content = fileNode.addNode("jcr:content", "nt:resource");
            content.setProperty("jcr:mimeType", "text/plain");
            content.setProperty("jcr:lastModified",  new GregorianCalendar());
            String strData = "Test Event Manager";
            content.setProperty("jcr:data",  new ByteArrayInputStream(strData.getBytes()));
            session.save();
            return fileNode;
        } catch (RepositoryException e) {
            return null;
        }
    }
    
    private class DMSFile extends AbstractFile<DMSFile> implements File {

    	public static final String DMS_FILE = "DMSFile";

    	public DMSFile(String workspace, String path) {
    		super(workspace, path);
    	}

    	public DMSFile(String workspace, String path, boolean isSystem) {
    		super(workspace, path, isSystem);
    	}

    	@Override
    	public DMSFile getParent() throws ItemNotFoundException,
    	AccessDeniedException, PathNotFoundException,
    	RepositoryException {
    		return null;
    	}

    	@Override
    	public String getObjectType() {
    		return DMS_FILE;
    	}
    	
    }
    
    /**
     * An instance of DMSFileListener
     */
    private class TestDMSFileListener extends AbstractEventListener<DMSFile, Integer> {

        private boolean fileCreated = false;
        private boolean fileUpdated = false;
        private boolean fileRemoved = false;
        
        @Override
        public void create(Event<DMSFile, Integer> event) {
            System.out.println("\n\nCreating dms file ====" +event.getSource().getPath()+ "\n\n");
            fileCreated = true;
        }

        @Override
        public void update(Event<DMSFile, Integer> event) {
            System.out.println("\n\nUpdating dms file ====" +event.getSource().getPath()+ "\n\n");
            fileUpdated = true;
        }

        @Override
        public void remove(Event<DMSFile, Integer> event) {
            System.out.println("\n\nRemoving dms file ====" +event.getSource().getPath()+ "\n\n");
            fileRemoved = true;
        }

        @Override
        public void onEvent(Event<DMSFile, Integer> event) throws Exception {
            int eventType = event.getData();
            switch(eventType) {
            case EventType.CREATED :
                create(event);
                break;
            case EventType.REMOVE :
                remove(event);
                break;
            case EventType.UPDATE :
                update(event);
                break;
            default :
                break;
            }
        }
        
    }
 
}
