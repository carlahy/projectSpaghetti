package comp207p.target;

public class DynamicVariableFolding {
    public int methodOne() {
        int a = 42;
        int b = (a + 764) * 3;
            a = b - 67;
        return b + 1234 - a;
        //b is calculated twice (in a=b-67 and in return)
    }

    public boolean methodTwo() {
        int x = 12345;
        int y = 54321;
        System.out.println(x < y);
        y = 0;
        return x > y;
        //The essential is what it returns, so maybe just remove the first y? Like, if it's not included in the return statement remove it? And also the print statement? That can be done at compile time.
    }

    public int methodThree() {
        int i = 0;
        int j = i + 3;
        i = j + 4;
        j = i + 5;
        return i * j;
        //i = 0 + 3 + 4 is calculated twice, once for i and once for i + 5
        //can put whole statement into one and return one load??
    }
    
    public int methodFour(){
        int a = 534245;
        int b = a - 1234;
        System.out.println((120298345 - a) * 38.435792873);
        for(int i = 0; i < 10; i++){
            System.out.println((b - a) * i);
        }
        a = 4;
        b = a + 2;
        return a * b;
        //fold b.
        //multiply an int with double
        //b-a is the same as a, so no need of b - remove it.
        //will return just one load --> a*b
    }
}