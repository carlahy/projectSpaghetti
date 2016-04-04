package comp207p.target;

public class DynamicVariableFolding {
    public int methodOne() {
        int a = 42;
        int b = (a + 764) * 3;
            a = b - 67;//2351
        return b + 1234 - a;//1301
    }

    public boolean methodTwo() {
        int x = 12345;
        int y = 54321;
        System.out.println(x < y);
        y = 0;
        return x > y;
    }

    public int methodThree() {
        int i = 0;
        int j = i + 3;
        i = j + 4;
        j = i + 5;
        return i * j;
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