package comp207p.target;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Test dynamic variable folding
 */

public class DynamicVariableFoldingTest
{
    DynamicVariableFolding dvf = new DynamicVariableFolding();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams()
    {
//        System.out.println("\tTest setup");

        System.setOut(new PrintStream(outContent));
//        System.out.println("\tTest setup end");
    }

    @After
    public void cleanUpStreams()
    {
//        System.out.println("\tTest cleanup");
        System.setOut(null);
    }

    @Test
    public void testMethodOne()
    {
//        System.out.println("\tTest 1");
//        System.out.println("\tDynamic Folding: MethodOne() " + dvf.methodOne());
        assertEquals(1301, dvf.methodOne());
    }

    @Test
    public void testMethodTwoOut()
    {
//        System.out.println("\tTest 2 out");
        dvf.methodTwo();
//        System.out.println("\tDynamic Folding: MethodTwo() out " + outContent.toString());
        assertEquals("true\n", outContent.toString());
    }

    @Test
    public void testMethodTwoReturn()
    {
//        System.out.println("\tTest 2 return");
//        System.out.println("\tDynamic Folding: MethodTwo() " + dvf.methodTwo());
        assertEquals(true, dvf.methodTwo());
    }

    @Test
    public void testMethodThree()
    {
//        System.out.println("\tTest 3");
//        System.out.println("\tDynamic Folding: MethodThree() " + dvf.methodThree());
        assertEquals(84, dvf.methodThree());
    }
    
    @Test
    public void testMethodFour()
    {
        System.out.println("\tTest 4");
//        System.out.println("\tDynamic Folding: MethodFour() " + dvf.methodFour());
        assertEquals(24, dvf.methodFour());
    }


}