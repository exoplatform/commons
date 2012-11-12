package org.exoplatform.services.deployment;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class TestContentInitializerService extends BaseCommonsTestCase {
	private static String EXO_SERVICES = "eXoServices";
	private static String EXO_CIS_LOG = "ContentInitializerServiceLog";
	private static String CONTENT_INIT = "ContentInitializerService";
	private ContentInitializerService contentInitializerService;

	public void setUp() throws Exception {
		super.setUp();
		// see file test-conteninitializerservice-configuration.xml
		contentInitializerService = getService(ContentInitializerService.class);
	}

	public void testStart() throws Exception {
		SessionProvider sessionProvider = null;
		try {
			sessionProvider = SessionProvider.createSystemProvider();
			ManageableRepository repository = repositoryService
					.getCurrentRepository();
			Session session = sessionProvider.getSession(repository
					.getConfiguration().getDefaultWorkspaceName(), repository);
			NodeHierarchyCreator nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
			Node serviceFolder = (Node) session.getItem(nodeHierarchyCreator
					.getJcrPath(EXO_SERVICES));
			assertNotNull(contentInitializerService);
			assertTrue(serviceFolder.hasNode(CONTENT_INIT));
			Node contentInitializerServiceNode = serviceFolder
					.getNode(CONTENT_INIT);
			assertTrue(contentInitializerServiceNode.hasNode(EXO_CIS_LOG));
			Node cisfFilelOG = contentInitializerServiceNode
					.getNode(EXO_CIS_LOG);
			assertTrue(cisfFilelOG.hasNode("jcr:content"));
			Node cisContentLog = cisfFilelOG.getNode("jcr:content");
			assertTrue(cisContentLog.getProperty("jcr:data").getString()
					.contains("successful"));
		} catch (RepositoryException e) {
		} finally {	
			sessionProvider.close();
		}

	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public static class DeploymentPluginTest extends DeploymentPlugin {

		public DeploymentPluginTest() {
			super();
		}

		private Log LOG = ExoLogger.getLogger(DeploymentPluginTest.class);

		@Override
		public void deploy(SessionProvider sessionProvider) throws Exception {
			LOG.info("deploying  DeploymentPluginTest ");
		}
	}

}
