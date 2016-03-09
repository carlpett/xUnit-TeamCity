package se.capeit.dev.xunittestrunner;

import org.junit.Assert;
import org.junit.Test;

public class RunnersTest {
    @Test
    public void testCorrectVersionReturned() {
        Assert.assertEquals("2.0.0", Runners.getRunner("2.0.0").version);
    }

    @Test
    public void testCorrectRunnerPath() {
        RunnerVersion runner = Runners.getRunner("1.9.2");
        Assert.assertEquals("xunit.console.clr4.x86.exe", runner.getRunnerPath(Runtime.dotNET40, Platforms.x86));
        Assert.assertEquals("xunit.console.clr4.exe", runner.getRunnerPath(Runtime.dotNET40, Platforms.MSIL));
        Assert.assertEquals("xunit.console.x86.exe", runner.getRunnerPath(Runtime.dotNET35, Platforms.x86));
        Assert.assertEquals("xunit.console.exe", runner.getRunnerPath(Runtime.dotNET35, Platforms.MSIL));
    }

    @Test
    public void testCorrectFallbackFor192() {
        RunnerVersion runner = Runners.getRunner("1.9.2");
        Assert.assertEquals("xunit.console.clr4.exe", runner.getRunnerPath("", ""));
    }
}