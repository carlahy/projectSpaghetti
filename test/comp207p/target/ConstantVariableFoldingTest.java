package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test constant variable folding
 */
public class ConstantVariableFoldingTest {

    ConstantVariableFolding cvf = new ConstantVariableFolding();

    @Test
    public void testMethodOne(){
//        System.out.println("\tConstant Folding: MethodOne() " + cvf.methodOne());
        assertEquals(3650, cvf.methodOne());
    }

    @Test
    public void testMethodTwo(){
//        System.out.println("\tConstant Folding: MethodTwo() " + cvf.methodTwo());
        assertEquals(1.67, cvf.methodTwo(), 0.001);
    }

    @Test
    public void testMethodThree(){
//        System.out.println("\tConstant Folding: MethodThree() " + cvf.methodThree());
        assertEquals(false, cvf.methodThree());
    }
    
    @Test
    public void testMethodFour(){
//        System.out.println("\tConstant Folding: MethodFour() " + cvf.methodFour());
        assertEquals(true, cvf.methodFour());
    }
    

}
