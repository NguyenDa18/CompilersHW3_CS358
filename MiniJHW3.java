class Main {
    public void main() {
        new Class2().exec();
    }

}

class Class1 extends Lib {
    int a;
    int c;
    public String myMethod(int b) {
        while(c < 10) {

            int a = c + b;
            c = c + a;
            break;
        }
        super.printInt(a);
        return "Jelly";
    }
}

class Class2 extends Class1 {
    public void exec() {
        myMethod(a);
    }
}

class Class3 extends Class1 {

}