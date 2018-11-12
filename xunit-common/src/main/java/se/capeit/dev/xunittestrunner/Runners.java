package se.capeit.dev.xunittestrunner;

import java.util.*;

public final class Runners {
    private static final TreeMap<String, RunnerVersion> AvailableRunners = new TreeMap<String, RunnerVersion>();

    public TreeMap<String, RunnerVersion> getAllRunners() { return AvailableRunners; }
    public Set<String> getSupportedVersions() { return AvailableRunners.descendingKeySet(); }
    public Set<String> getSupportedRuntimes() {
        HashSet<String> runtimes = new HashSet<String>();
        for(Map.Entry<String, RunnerVersion> runner : AvailableRunners.entrySet()) {
            for(String runtime : runner.getValue().supportedRuntimes)
                runtimes.add(runtime);
        }
        return runtimes;
    }
    public Set<String> getSupportedPlatforms() {
        HashSet<String> platforms = new HashSet<String>();
        for(Map.Entry<String, RunnerVersion> runner : AvailableRunners.entrySet()) {
            for(String platform : runner.getValue().supportedPlatforms)
                platforms.add(platform);
        }
        return platforms;
    }

    public static RunnerVersion getRunner(String version) {
        return AvailableRunners.get(version);
    }

    static {
        AvailableRunners.put("1.9.2", new RunnerVersion("1.9.2",
                new String[]{Runtime.dotNET35, Runtime.dotNET40},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (runtime.equals(Runtime.dotNET40) || runtime.equals(""))
                    sb.append(".clr4");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });

        AvailableRunners.put("2.0.0", new RunnerVersion("2.0.0",
                new String[]{Runtime.dotNET45},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });

        AvailableRunners.put("2.1.0", new RunnerVersion("2.1.0",
                new String[]{Runtime.dotNET45},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });

		AvailableRunners.put("2.2.0", new RunnerVersion("2.2.0",
                new String[]{Runtime.dotNET45},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });

        AvailableRunners.put("2.3.x", new RunnerVersion("2.3.x",
                new String[]{Runtime.dotNET45},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });

        AvailableRunners.put("2.4.x", new RunnerVersion("2.4.x",
                new String[]{Runtime.dotNET45},
                new String[]{Platforms.x86, Platforms.MSIL}) {
            @Override
            public String getRunnerPath(String runtime, String platform) {
                StringBuilder sb = new StringBuilder();
                sb.append("xunit.console");
                if (platform.equals(Platforms.x86))
                    sb.append(".x86");
                sb.append(".exe");
                return sb.toString();
            }
        });
    }
}

final class Runtime {
    public static final String dotNET35 = ".NET 3.5";
    public static final String dotNET40 = ".NET 4.0";
    public static final String dotNET45 = ".NET 4.5";
}
final class Platforms {
    public static final String MSIL = "AnyCPU/MSIL";
    public static final String x86 = "x86";
    public static final String x64 = "x64";
}