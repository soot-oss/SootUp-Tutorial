class Start {

  public static void main(String[] args) {
    A a = new D();
    A b = new B();
    start(b);
  }
  public static void start(A a) {
    a.m();
  }
}

class A {
  public void m(){

  }
}
class B  extends A{
  public void m(){

  }
}
class C  extends B{
  public void m(){

  }
}

class D  extends A{
  public void m(){

  }
}