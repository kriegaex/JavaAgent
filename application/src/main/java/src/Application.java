package src;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.UUID;

public class Application {
  private static final Random RANDOM = new Random();

  public static void main(String[] args) throws InterruptedException {
    System.out.println(ManagementFactory.getRuntimeMXBean().getName());
    while (true) {
      System.out.println(Long.toHexString(RANDOM.nextLong()));
      new Test().doSomething(11, "kjjgdfk");
      System.out.println(UUID.randomUUID());
      Thread.sleep(1000);
    }
  }

  static class Test {
    void doSomething(int id, String name) {
      System.out.println("id = " + id + ", name = " + name);
    }
  }
}
