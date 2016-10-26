<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="se.capeit.dev.xunittestrunner.StringConstants" />
<jsp:useBean id="runners" class="se.capeit.dev.xunittestrunner.Runners" />

<l:settingsGroup title="Runner Parameters">
    <tr>
        <th>
            <label for="${constants.parameterName_XUnitVersion}">XUnit version: </label>
        </th>
        <td>
            <props:selectProperty
                    name="${constants.parameterName_XUnitVersion}"
                    enableFilter="true"
                    id="xunit-version-selector"
                    className="mediumField">
                <c:forEach items="${runners.supportedVersions}" var="ver">
                    <props:option value="${ver}">${ver}</props:option>
                </c:forEach>
            </props:selectProperty>
        </td>
    </tr>
    <tr>
        <th rowspan="2">
            <label>.NET Runtime: </label>
        </th>
        <td>
            <label for="${constants.parameterName_Platform}" class="fixedLabel">Platform:</label>
            <props:selectProperty
                    name="${constants.parameterName_Platform}"
                    enableFilter="true"
                    id="xunit-platform"
                    className="mediumField version-dependent">
                <c:forEach items="${runners.supportedPlatforms}" var="platform">
                    <props:option value="${platform}">${platform}</props:option>
                </c:forEach>
            </props:selectProperty>
        </td>
    </tr>
    <tr>
        <td>
            <label for="${constants.parameterName_RuntimeVersion}" class="fixedLabel">Version:</label>
            <props:selectProperty
                    name="${constants.parameterName_RuntimeVersion}"
                    enableFilter="true"
                    id="xunit-runtime-version"
                    className="mediumField version-dependent">
                <c:forEach items="${runners.supportedRuntimes}" var="runtime">
                    <props:option value="${runtime}">${runtime}</props:option>
                </c:forEach>
            </props:selectProperty>
        </td>
    </tr>
    <tr>
            <th>
                <label for="${constants.parameterName_CommandLineArguments}">Command line arguments:</label>
            </th>
            <td>
                <props:textProperty
                        name="${constants.parameterName_CommandLineArguments}"
                        className="longField"
                        />
                <span class="error" id="error_${constants.parameterName_CommandLineArguments}"></span>
                <span class="smallNote"><pre>Valid options:
  -nologo                : do not show the copyright message
  -nocolor               : do not output results with colors
  -noappdomain           : do not use app domains to run test code
  -failskips             : convert skipped tests into failures
  -parallel option       : set parallelization based on option
                         :   none        - turn off all parallelization
                         :   collections - only parallelize collections
                         :   assemblies  - only parallelize assemblies
                         :   all         - parallelize assemblies & collections
  -maxthreads count      : maximum thread count for collection parallelization
                         :   default   - run with default (1 thread per CPU thread)
                         :   unlimited - run with unbounded thread count
                         :   (number)  - limit task thread pool size to 'count'
  -noshadow              : do not shadow copy assemblies
  -wait                  : wait for input after completion
  -diagnostics           : enable diagnostics messages for all test assemblies
  -debug                 : launch the debugger to debug the tests
  -serialize             : serialize all test cases (for diagnostic purposes only)
  -trait "name=value"    : only run tests with matching name/value traits
                         : if specified more than once, acts as an OR operation
  -notrait "name=value"  : do not run tests with matching name/value traits
                         : if specified more than once, acts as an AND operation
  -method "name"         : run a given test method (should be fully specified;
                         : i.e., 'MyNamespace.MyClass.MyTestMethod')
                         : if specified more than once, acts as an OR operation
  -class "name"          : run all methods in a given test class (should be fully
                         : specified; i.e., 'MyNamespace.MyClass')
                         : if specified more than once, acts as an OR operation
  -namespace "name"      : run all methods in a given namespace (i.e.,
                         : 'MyNamespace.MySubNamespace')
                         : if specified more than once, acts as an OR operation

Reporters: (optional, choose only one)
  -appveyor              : forces AppVeyor CI mode (normally auto-detected)
  -quiet                 : do not show progress messages
  -teamcity              : forces TeamCity mode (normally auto-detected)
  -verbose               : show verbose progress messages</pre></span>
            </td>
        </tr>
    <tr>
        <th>
            <label for="${constants.parameterName_IncludedAssemblies}">Assemblies containing tests: </label>
        </th>
        <td>
            <props:multilineProperty
                    name="${constants.parameterName_IncludedAssemblies}"
                    className="longField"
                    linkTitle="Edit assembly files include list"
                    rows="3"
                    cols="49"
                    expanded="${true}"
                    />
            <span class="error" id="error_${constants.parameterName_IncludedAssemblies}"></span>
            <span class="smallNote">
                Enter comma- or newline-separated paths to assembly files relative to checkout directory.
                Wildcards are supported.
            </span>
        </td>
    </tr>

    <tr>
        <th>
            <label for="${constants.parameterName_ExcludedAssemblies}">Exclude these assemblies: </label>
        </th>
        <td>
            <props:multilineProperty
                    name="${constants.parameterName_ExcludedAssemblies}"
                    className="longField"
                    linkTitle="Edit assembly files exclude list"
                    rows="3"
                    cols="49"
                    expanded="${not empty propertiesBean.properties[constants.parameterName_ExcludedAssemblies]}"
                    />
            <span class="error" id="error_${constants.parameterName_ExcludedAssemblies}"></span>
            <span class="smallNote">
                Enter comma- or newline-separated paths to assembly files relative to checkout directory.
                Wildcards are supported.
            </span>
        </td>
    </tr>
</l:settingsGroup>

<script type="text/javascript">
        var featureMatrix = {
            <c:forEach items="${runners.allRunners}" var="runner" varStatus="outerLoop">
            "${runner.key}": {
                "xunit-platform": [<c:forEach items="${runner.value.supportedPlatforms}" var="platform" varStatus="innerLoop">"${platform}"${!innerLoop.last ? ',' : ''}</c:forEach>],
                "xunit-runtime-version": [<c:forEach items="${runner.value.supportedRuntimes}" var="runtime" varStatus="innerLoop">"${runtime}"${!innerLoop.last ? ',' : ''}</c:forEach>]
            }${!outerLoop.last ? ',' : ''}
            </c:forEach>
        };

        $j(document).ready(function() {
            $j("select.version-dependent").each(function(idx) {
                var elem = $j(this);
                elem.data('options', elem.find("option").detach());
            });

            $j("#xunit-version-selector").change(function() {
                var selected = $j(":selected", this).val();
                var features = featureMatrix[selected];
                for(var feature in features) {
                    var enabled = features[feature];
                    console.log(feature + ':' + enabled);
                    var featureElement = $j('#' + feature);

                    if(featureElement.is("select")) {
                        var options = featureElement.data('options').filter(function(idx, elem) { 
                            return $j.inArray(elem.text, enabled) != -1;
                        });
                        featureElement.empty() // Remove all existing options
                                      .append(options);

                        // TODO: Add some debug element in case featureElement is now empty
                    }
                    else {
                        // TODO: Toggle visibility for non-dropdowns 
                    }
                }
                
                $j("select.version-dependent").ufd('changeOptions');
            });

            $j("#xunit-version-selector").change();
        });
</script>