package se.capeit.dev.xunittestrunner;

import jetbrains.buildServer.dotNet.DotNetConstants;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class XUnitTestRunType extends RunType {
    private final PluginDescriptor pluginDescriptor;

    public XUnitTestRunType(@NotNull final RunTypeRegistry runTypeRegistry, final PluginDescriptor pluginDescriptor)
    {
        this.pluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return StringConstants.RunTypeName;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "xUnit";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Runs tests written in xUnit";
    }

    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new PropertiesProcessor() {
            public Collection<InvalidProperty> process(Map<String, String> properties) {
                ArrayList<InvalidProperty> toReturn = new ArrayList<InvalidProperty>();
                if (!properties.containsKey(StringConstants.ParameterName_IncludedAssemblies) ||
                        properties.get(StringConstants.ParameterName_IncludedAssemblies).isEmpty())
                    toReturn.add(new InvalidProperty(StringConstants.ParameterName_IncludedAssemblies,
                            "Please enter what assemblies should be included"));

                // TODO: Validate runner supports platform/runtime

                return toReturn;
            }
        };
    }

    @Override
    public String getEditRunnerParamsJspFilePath() {
        return pluginDescriptor.getPluginResourcesPath("editRunnerParameters.jsp");
    }

    @Override
    public String getViewRunnerParamsJspFilePath() {
        return pluginDescriptor.getPluginResourcesPath("viewRunnerParameters.jsp");
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        HashMap<String, String> defaults = new HashMap<String, String>();
        defaults.put(StringConstants.ParameterName_XUnitVersion, "2.1.0");
        defaults.put(StringConstants.ParameterName_Platform, Platforms.MSIL);
        defaults.put(StringConstants.ParameterName_RuntimeVersion, Runtime.dotNET45);

        return defaults;
    }

    @NotNull
    @Override
    public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
        List<Requirement> requirements = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));

        String runtime = runParameters.get(StringConstants.ParameterName_RuntimeVersion);
        String platform = runParameters.get(StringConstants.ParameterName_Platform);
        // 1.0 of the plugin did not set runtime/platform, because only a single version was supported
        // For upgrades we need to manually set runtime/platform
        if(runtime == null || platform == null) {
            String xUnitVersion = runParameters.get(StringConstants.ParameterName_XUnitVersion);
            switch(xUnitVersion) {
                case "1.9.2":
                    runtime = Runtime.dotNET40;
                    platform = Platforms.MSIL;
                    break;
                case "2.0.0":
                    runtime = Runtime.dotNET45;
                    platform = Platforms.MSIL;
                    break;
                default:
                    // The famous last words, "This should never happen". But, if it does, let's just ask the user to
                    // re-set the values in their build config
                    requirements.add(new Requirement("xUnit-plugin: Invalid configuration, unable to build. Please verify build step settings", null, RequirementType.EXISTS));
                    return requirements;
            }
        }

        String frameworkMatcher = null;
        if (runtime.equals(Runtime.dotNET35)) {
            frameworkMatcher = "3.5"; // Match just 3.5
        }
        else if (runtime.equals(Runtime.dotNET40)) {
            frameworkMatcher = "4.[0-9\\.]+"; // Match any 4.x
        }
        else if (runtime.equals(Runtime.dotNET45)) {
            frameworkMatcher = "4.[56](\\.[0-9]+)?"; // Match 4.5+
        }

        String platformMatcher = null;
        if (platform.equals(Platforms.MSIL)) {
            platformMatcher = "(x86|x64)";
        }
        else {
            platformMatcher = platform;
        }

        requirements.add(new Requirement("Exists=>DotNetFramework" + frameworkMatcher + "_" + platformMatcher, null, RequirementType.EXISTS));
        requirements.add(new Requirement("teamcity.tool." + StringConstants.ToolName, null, RequirementType.EXISTS));

        return requirements;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Version ");
        sb.append(parameters.get(StringConstants.ParameterName_XUnitVersion));

        sb.append(" (");
        sb.append(parameters.get(StringConstants.ParameterName_Platform));
        sb.append("/");
        sb.append(parameters.get(StringConstants.ParameterName_RuntimeVersion));
        sb.append(")");

        sb.append("\nCommand line arguments: ");
        sb.append(parameters.get(StringConstants.ParameterName_CommandLineArguments));

        sb.append("\nIncluded assemblies: ");
        sb.append(parameters.get(StringConstants.ParameterName_IncludedAssemblies));

        String excluded = parameters.get(StringConstants.ParameterName_ExcludedAssemblies);
        if(excluded != null && !excluded.trim().isEmpty()) {
            sb.append("\nExcluded assemblies: ");
            sb.append(excluded);
        }
        return sb.toString();
    }
}
