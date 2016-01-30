package se.capeit.dev.xunittestrunner;

public abstract class RunnerVersion {
    public final String version;
    public String[] supportedRuntimes;
    public String[] supportedPlatforms;

    public RunnerVersion(String version, String[] supportedRuntimes, String[] supportedPlatforms) {
        this.version = version;
        this.supportedRuntimes = supportedRuntimes;
        this.supportedPlatforms = supportedPlatforms;
    }

    public abstract String getRunnerPath(String runtime, String platform);

    public String[] getSupportedRuntimes() { return supportedRuntimes; }
    public String[] getSupportedPlatforms() { return supportedPlatforms; }
}

