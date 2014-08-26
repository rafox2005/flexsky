package experimentPlanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rlibardi
 */


public class ScenarioParserTest {
    
    String pathToXML = "/home/rlibardi/NetBeansProjects/safestore-leicester/flexsky/test/experimentPlanner/scenarioex3.xml";
   
    
    public ScenarioParserTest() {
        
         }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class ScenarioParser.
     */
    @Test
    public void testParse() throws Exception {
        ScenarioParser.parse(this.pathToXML);
    }
    
}
