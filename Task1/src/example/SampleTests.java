package example;

import runner.annotations.*;

public class SampleTests {

    @BeforeSuite
    public static void initSuite() {
        System.out.println("[BeforeSuite] Initialize shared resources");
    }

    @AfterSuite
    public static void tearDownSuite() {
        System.out.println("[AfterSuite] Cleanup shared resources");
    }

    @BeforeTest
    public void beforeEach() {
        System.out.println("  [BeforeTest] setup");
    }

    @AfterTest
    public void afterEach() {
        System.out.println("  [AfterTest] teardown");
    }

    @Test(priority = 10)
    public void highPriorityTest() {
        System.out.println("    [Test p10] highPriorityTest executed");
    }

    @Test(priority = 1)
    public void lowPriorityTest() {
        System.out.println("    [Test p1] lowPriorityTest executed");
    }

    @Test
    public void defaultPriorityTest() {
        System.out.println("    [Test p5] defaultPriorityTest executed");
    }

    @Test(priority = 7)
    @CsvSource("10, Java, 20, true")
    public void csvDrivenTest(int a, String b, int c, boolean d) {
        System.out.println("    [Test p7] csvDrivenTest args: a=" + a + ", b=" + b + ", c=" + c + ", d=" + d);
        if (a + c != 30 || !d || !"Java".equals(b)) {
            throw new AssertionError("csvDrivenTest failed");
        }
    }
}


