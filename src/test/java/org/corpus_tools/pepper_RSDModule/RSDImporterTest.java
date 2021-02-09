package org.corpus_tools.pepper_RSDModule;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for testing the  * {@link RSDImporter} class. 
 * Please note, that the test class is derived from {@link PepperImporterTest}.
 * 
 * @author Amir Zeldes
 */
public class RSDImporterTest {

    
	private RSD2SaltMapper fixture = null;

        RSD2SaltMapper getFixture() {
		return fixture;
	}

	private void setFixture(RSD2SaltMapper fixture) {
		this.fixture = fixture;
	}

	@Before
	public void setUp() {
		this.setFixture(new RSD2SaltMapper());
		
		//URI propertiesLoc = URI.createFileURI("./src/test/resources/RSDImporter.properties");
		//getFixture().getProperties().setPropertyValues(new File(propertiesLoc.toFileString()));
		SDocument sDoc = SaltFactory.createSDocument();
		SaltFactory.createIdentifier(sDoc, "doc1");
		getFixture().setDocument(sDoc);
		getFixture().getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());

	}
        
        
        @Test
	public void testRSDAnnotation()
	{
	  getFixture().setResourceURI(URI.createFileURI("src/test/resources/GUM_news_expo.rsd"));
	  getFixture().mapSDocument();
	  
	  SDocumentGraph dg = getFixture().getDocument().getDocumentGraph();

          // check token count
	  assertEquals(751, dg.getTokens().size());
          // checks that all nodes are contained
          assertEquals(751+67+1, dg.getNodes().size());
          // checks that all spans (subclass of nodes) are contained
          assertEquals(67, dg.getSpans().size());
          // checks that all structures (subclass of nodes) are contained
          assertEquals(0, dg.getStructures().size());
          // checks that all pointing relations (subclass of relations) are
          // contained
          assertEquals(66, dg.getPointingRelations().size());          
        }
        

}