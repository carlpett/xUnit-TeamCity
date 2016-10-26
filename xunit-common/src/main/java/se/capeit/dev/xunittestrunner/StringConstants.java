package se.capeit.dev.xunittestrunner;

public final class StringConstants {
    public static final String RunTypeName = "xUnitRunner";
    public static final String ToolName = "xunit-runner"; // Should mirror the xunit-runner artifactId

    public static final String ParameterName_XUnitVersion = "xUnitVersion";
    public static final String ParameterName_CommandLineArguments = "commandLineArguments";
    public static final String ParameterName_IncludedAssemblies = "includedAssemblies";
    public static final String ParameterName_ExcludedAssemblies = "excludedAssemblies";
    public static final String ParameterName_Platform = "runnerPlatform";
    public static final String ParameterName_RuntimeVersion = "runnerRuntimeVersion";

    // Getter methods for JSP pages
    public String getParameterName_XUnitVersion() {
        return ParameterName_XUnitVersion;
    }
    public String getParameterName_CommandLineArguments() {
        return ParameterName_CommandLineArguments;
    }
    public String getParameterName_IncludedAssemblies() {
        return ParameterName_IncludedAssemblies;
    }
    public String getParameterName_ExcludedAssemblies() {
        return ParameterName_ExcludedAssemblies;
    }
    public String getParameterName_Platform() {
        return ParameterName_Platform;
    }
    public String getParameterName_RuntimeVersion() {
        return ParameterName_RuntimeVersion;
    }
}
