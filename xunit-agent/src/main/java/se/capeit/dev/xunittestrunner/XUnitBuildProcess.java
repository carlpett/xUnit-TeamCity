package se.capeit.dev.xunittestrunner;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class XUnitBuildProcess extends FutureBasedBuildProcess {
    private final AgentRunningBuild buildingAgent;
    private final BuildRunnerContext context;
    private HashMap<Process, String> processes = new HashMap<>();

    public XUnitBuildProcess(@NotNull final BuildRunnerContext context) {
        super(context);

        this.context = context;
        this.buildingAgent = context.getBuild();
    }

    private String getParameter(@NotNull final String parameterName) {
        final String value = context.getRunnerParameters().get(parameterName);
        if (value == null || value.trim().length() == 0) return "";
        return value.trim();
    }

    private List<String> getAssemblies(final String rawAssemblyParameter) {
        String withSlashesFixed = rawAssemblyParameter.replace('\\', '/');
        List<String> assemblies = StringUtil.split(withSlashesFixed, true, ',', ';', '\n', '\r');
        return assemblies;
    }

    protected void cancelBuild() {
        for (Map.Entry<Process, String> p : processes.entrySet()) {
            p.getKey().destroy();
        }
        processes.clear();
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

            List<String> assemblies = getAssemblies(getParameter(StringConstants.ParameterName_IncludedAssemblies));
            List<String> excludedAssemblies = getAssemblies(getParameter(StringConstants.ParameterName_ExcludedAssemblies));
            excludedAssemblies.add("**/obj/**"); // We always exclude **/obj/**

            BuildFinishedStatus status = BuildFinishedStatus.FINISHED_SUCCESS;

            // Find the files, and run them through the test runner
            AntPatternFileFinder finder = new AntPatternFileFinder(
                    CollectionsUtil.toStringArray(assemblies),
                    CollectionsUtil.toStringArray(excludedAssemblies),
                    SystemInfo.isFileSystemCaseSensitive);
            File[] assemblyFiles = finder.findFiles(context.getWorkingDirectory());
            if (assemblyFiles.length == 0) {
                logger.warning("No assemblies were matched - no tests will be run!");
            }

            for (File assembly : assemblyFiles) {
                String activityBlockName = "Testing " + assembly.getName();
                logger.activityStarted(activityBlockName, assembly.getAbsolutePath(), DefaultMessagesInfo.BLOCK_TYPE_MODULE);

                String filePath = assembly.getAbsolutePath();
                String commandLineFlags = getCommandLineFlags(version);
                logger.message("Commandline: " + runnerPath + " " + filePath + " " + commandLineFlags);
                ProcessBuilder processBuilder = new ProcessBuilder(runnerPath, filePath, commandLineFlags);

                // Copy environment variables
                Map<String, String> env = processBuilder.environment();
                for (Map.Entry<String, String> kvp : context.getBuildParameters().getEnvironmentVariables().entrySet()) {
                    env.put(kvp.getKey(), kvp.getValue());
                }

                Process testRunnerProcess = processBuilder.start();
                processes.put(testRunnerProcess, activityBlockName);

                redirectStreamToLogger(testRunnerProcess.getInputStream(), new RedirectionTarget() {
                    public void redirect(String s) {
                        logger.message(s);
                    }
                });
                redirectStreamToLogger(testRunnerProcess.getErrorStream(), new RedirectionTarget() {
                    public void redirect(String s) {
                        logger.warning(s);
                    }
                });

                while (true) {
                    int liveProcessCount = 0;
                    for (Map.Entry<Process, String> p : processes.entrySet()) {
                        if (isRunning(p.getKey())) {
                            ++liveProcessCount;
                        }
                    }
                    if (liveProcessCount < 5) break;
                    Thread.sleep(100);
                }
            }

            for (Map.Entry<Process, String> p : processes.entrySet()) {
                int exitCode = p.getKey().waitFor();
                if (exitCode != 0) {
                    logger.warning("Test runner exited with non-zero status!");
                    status = BuildFinishedStatus.FINISHED_FAILED;
                }
                logger.activityFinished(p.getValue(), DefaultMessagesInfo.BLOCK_TYPE_MODULE);
            }
            return status;
        } catch (Exception e) {
            logger.message("Failed to run tests");
            logger.exception(e);
            return BuildFinishedStatus.FINISHED_FAILED;
        } finally {
            processes.clear();
        }
    }

    boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
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
        if (majorVersion == '1')
            return "/teamcity";
        return "-teamcity";
    }
}