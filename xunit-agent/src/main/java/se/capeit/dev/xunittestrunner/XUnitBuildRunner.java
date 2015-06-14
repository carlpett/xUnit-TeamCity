package se.capeit.dev.xunittestrunner;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class XUnitBuildRunner implements AgentBuildRunner, AgentBuildRunnerInfo {
    @NotNull
    public BuildProcess createBuildProcess(AgentRunningBuild agentRunningBuild,
                                           BuildRunnerContext buildRunnerContext)
            throws RunBuildException {
        return new XUnitBuildProcess(buildRunnerContext);
    }

    @NotNull
    public AgentBuildRunnerInfo getRunnerInfo() {
        return this;
    }

    @NotNull
    public String getType() {
        return StringConstants.RunTypeName;
    }

    public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
        return true;
    }
}

