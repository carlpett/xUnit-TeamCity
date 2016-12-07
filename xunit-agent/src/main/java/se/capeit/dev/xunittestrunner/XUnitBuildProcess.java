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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private int tryParseInt(String stringValue, int defaultValue) {
        try {
            return Integer.parseInt(stringValue);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public BuildFinishedStatus call() throws Exception {
        try {
            String version = getParameter(StringConstants.ParameterName_XUnitVersion);
            RunnerVersion runner = Runners.getRunner(version);
            String runtime = getParameter(StringConstants.ParameterName_RuntimeVersion);
            String platform = getParameter(StringConstants.ParameterName_Platform);
            logger.message("Runner parameters { Version = " + version + ", runtime = " + runtime + ", platform = " + platform + "}");

            int numberOfParallelProcesses = tryParseInt(getParameter(StringConstants.ParameterName_NumberOfParallelProcesses), 1);
            logger.message("Number of parallel processes is set to: " + numberOfParallelProcesses);

            File agentToolsDirectory = buildingAgent.getAgentConfiguration().getAgentToolsDirectory();
            String runnerPath = new File(agentToolsDirectory, "xunit-runner\\bin\\" + version + "\\" + runner.getRunnerPath(runtime, platform)).getPath();
            logger.message("Starting test runner at " + runnerPath);

            List<String> assemblies = getAssemblies(getParameter(StringConstants.ParameterName_IncludedAssemblies));
            String commandLineArguments = getParameter(StringConstants.ParameterName_CommandLineArguments);
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
                logger.message("Commandline: " + runnerPath + " " + filePath + " " + commandLineFlags + " " + commandLineArguments);

                List<String> commandLine = new ArrayList<>();
                commandLine.add(runnerPath);
                commandLine.add(filePath);
                commandLine.add(commandLineFlags);

                Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(commandLineArguments);
                while (m.find())
                    commandLine.add(m.group(1).replace("\"", ""));

                ProcessBuilder processBuilder = new ProcessBuilder(commandLine);

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
                    if (liveProcessCount < numberOfParallelProcesses) break;
                    Thread.sleep(100);
                }
            }

            for (Map.Entry<Process, String> p : processes.entrySet()) {
                int exitCode = p.getKey().waitFor();
                if (version.charAt(0) == '2' && version.charAt(2) == '2') {
                    // From 2.2 the exit code actually indicates if there was an error with the command line / runtime. https://github.com/xunit/xunit/issues/659
                    if(exitCode > 1) {
                        logger.warning("Test runner exited with status larger then 1! Actuall status code was " + exitCode + " expected status code was 0 or 1");
                        status = BuildFinishedStatus.FINISHED_FAILED; 
                    }   
                }
                // Checking the exit code on versions below 2.2 is actually useless, as they break the TeamCity function to ignore failed tests.
                // The exit code on older versions of xunit always indicates the number of failed tests.
                
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