<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="se.capeit.dev.xunittestrunner.StringConstants" />

<l:settingsGroup title="Runner Parameters">
    <tr>
        <th>
            <label for="${constants.parameterName_XUnitVersion}">XUnit version: </label>
        </th>
        <td>
            <props:selectProperty
                    name="${constants.parameterName_XUnitVersion}"
                    enableFilter="true"
                    className="mediumField">
                <props:option value="2.0.0">2.0.0</props:option>
                <props:option value="1.9.2">1.9.2</props:option>
            </props:selectProperty>
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