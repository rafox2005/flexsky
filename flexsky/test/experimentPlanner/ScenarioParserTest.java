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
    
    String pathToXML = "/home/rlibardi/NetBeansProjects/flexsky/flexsky/test/experimentPlanner/scenario-sac.xml";
   
    
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
        Scenario test = ScenarioParser.parse(this.pathToXML);
        ScenarioExecutor ex = ScenarioExecutor.getInstance();
        ex.execute(test);
    }
    
}
