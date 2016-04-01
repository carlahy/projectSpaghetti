package comp207p.target;

public class ConstantVariableFolding
{
    public int methodOne(){
        int a = 62;
        int b = (a + 764) * 3;
        return b + 1234 - a;
        //calculate b, then return and make one load as return
    }

    public double methodTwo(){
        double i = 0.67;
        int j = 1;
        return i + j;
        //just make one load in compile time (return)
        //consider the case of adding double and int
    }

    public boolean methodThree(){
        int x = 12345;
        int y = 54321;
        return x > y;
        //calculate the boolean and just make one load
    }

    public boolean methodFour(){
        long x = 4835783423L;
        long y = 400000;
        long z = x + y;
        return x < y;
        //can remove z --> unused
    }

}