package se.capeit.dev.xunittestrunner;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.AntPatternFileFinder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

class XUnitBuildProcess extends FutureBasedBuildProcess {
    private final AgentRunningBuild buildingAgent;
    private final BuildRunnerContext context;

    public XUnitBuildProcess(@NotNull final BuildRunnerContext context) {
        super(context);

        this.context = context;
        this.buildingAgent = context.getBuild();
    }

    private String getParameter(@NotNull final String parameterName)
    {
        final String value = context.getRunnerParameters().get(parameterName);
        if (value == null || value.trim().length() == 0) return "";
        return value.trim();
    }

    public BuildFinishedStatus call() throws Exception {
        try {
            String version = getParameter(StringConstants.ParameterName_XUnitVersion);
            RunnerVersion runner = Runners.getRunner(version);
            String runtime = getParameter(StringConstants.ParameterName_RuntimeVersion);
            String platform = getParameter(StringConstants.ParameterName_Platform);
            logger.message("Runner parameters { Version = " + version + ", runtime = " + runtime + ", platform = " + platform + "}");

            File agentToolsDirectory = buildingAgent.getAgentConfiguration().getAgentToolsDirectory();
            String runnerPath = new File(agentToolsDirectory, "xunit-runner\\bin\\" + version + "\\" + runner.getRunnerPath(runtime, platform)).getPath();
            logger.message("Starting test runner at " + runnerPath);

            String rawAssemblyParameters = getParameter(StringConstants.ParameterName_IncludedAssemblies);
            String[] assemblies = rawAssemblyParameters.split(",|;|\n");

            // Build the exclusion list. We always exclude **/obj/**, but if the user has added other patterns, add those
            String rawUserExcludedAssemblies = getParameter(StringConstants.ParameterName_ExcludedAssemblies);
            String[] userExcludedAssemblies = rawUserExcludedAssemblies.split(",|;|\n");
            String[] excludedAssemblies = new String[1 + userExcludedAssemblies.length];
            excludedAssemblies[0] = "**/obj/**"; // Always exclude obj
            for (int i = 0; i < userExcludedAssemblies.length; i++)
                excludedAssemblies[i + 1] = userExcludedAssemblies[i];

            BuildFinishedStatus status = BuildFinishedStatus.FINISHED_SUCCESS;

            // Find the files, and run them through the test runner
            AntPatternFileFinder finder = new AntPatternFileFinder(assemblies, excludedAssemblies, true);
            for(File assembly : finder.findFiles(context.getWorkingDirectory())) {
                String activityBlockName = "Testing " + assembly.getName();
                logger.activityStarted(activityBlockName, assembly.getAbsolutePath(), DefaultMessagesInfo.BLOCK_TYPE_MODULE);

                String filePath = assembly.getAbsolutePath();
                String commandLineFlags = getCommandLineFlags(version);
                Process process = new ProcessBuilder(runnerPath, filePath, commandLineFlags).start();
                redirectStreamToLogger(process.getInputStream(), new RedirectionTarget() {
                    public void redirect(String s) {
                        logger.message(s);
                    }
                });
                redirectStreamToLogger(process.getErrorStream(), new RedirectionTarget() {
                    public void redirect(String s) {
                        logger.warning(s);
                    }
                });

                int exitCode = process.waitFor();
                if(exitCode != 0) {
                    logger.warning("Test runner exited with non-zero status!");
                    status = BuildFinishedStatus.FINISHED_FAILED; 
                }

                logger.activityFinished(activityBlockName, DefaultMessagesInfo.BLOCK_TYPE_MODULE);
            }

            return status;
        }
        catch(Exception e) {
            logger.message("Failed to run tests");
            logger.exception(e);
            return BuildFinishedStatus.FINISHED_FAILED;
        }
    }

    private interface RedirectionTarget {
        void redirect(String s);
    }
    private void redirectStreamToLogger(final InputStream s, final RedirectionTarget target) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(s);
                while (sc.hasNextLine()) {
                    target.redirect(sc.nextLine());
                }
            }
        }).start();
    }

    private String getCommandLineFlags(String version) {
        // xUnit 2.0 changed format of commandline arguments from /flag to -flag.
        // This is quite crude at the moment, but does the job.
        // TODO: Migrate this into RunnerVersion or similar
        char majorVersion = version.charAt(0);
        if(majorVersion == '1')
            return "/teamcity";
        return "-teamcity";
    }
}