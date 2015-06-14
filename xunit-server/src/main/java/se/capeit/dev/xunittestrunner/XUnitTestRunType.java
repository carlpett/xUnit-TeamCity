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
        return null;
    }

    @NotNull
    @Override
    public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
        List<Requirement> requirements = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));
        // The console runner is compiled to AnyCPU, just pick x32 to be compatible with both 32/64 bit
        requirements.add(new Requirement(DotNetConstants.DOTNET_FRAMEWORK_4_0 + DotNetConstants.X32, null, RequirementType.EXISTS));
        requirements.add(new Requirement("teamcity.tool." + StringConstants.ToolName, null, RequirementType.EXISTS));

        return requirements;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Version ");
        sb.append(parameters.get(StringConstants.ParameterName_XUnitVersion));
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
