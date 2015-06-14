<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
    XUnit version: <strong><props:displayValue name="xUnitVersion" /></strong>
    Assemblies: <strong><props:displayValue name="includedAssemblies" /></strong>
    Excluded assemblies: <strong><props:displayValue name="excludedAssemblies" /></strong>
</div>