package se.capeit.dev.xunittestrunner;

public final class StringConstants {
    public static final String RunTypeName = "xUnitRunner";
    public static final String ToolName = "xunit-runner"; // Should mirror the xunit-runner artifactId

    public static final String ParameterName_XUnitVersion = "xUnitVersion"; // Should mirror the xunit-runner artifactId
    public static final String ParameterName_IncludedAssemblies = "includedAssemblies";
    public static final String ParameterName_ExcludedAssemblies = "excludedAssemblies";

    // Getter methods for JSP pages
    public String getParameterName_XUnitVersion() {
        return ParameterName_XUnitVersion;
    }
    public String getParameterName_IncludedAssemblies() {
        return ParameterName_IncludedAssemblies;
    }
    public String getParameterName_ExcludedAssemblies() {
        return ParameterName_ExcludedAssemblies;
    }
}
