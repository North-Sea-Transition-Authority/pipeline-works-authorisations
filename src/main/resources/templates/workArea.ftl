<#include 'layout.ftl'>

<#-- @ftlvariable name="startPwaApplicationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Work area" pageHeading="Work area" topNavigation=true>

    <@fdsAction.link linkText="Start PWA application" linkUrl=springUrl(startPwaApplicationUrl) linkClass="govuk-button"/>

    <@fdsAction.link linkText="Start Prototype PWA application" linkUrl=springUrl(prototypeApplicationUrl) linkClass="govuk-button"/>

</@defaultPage>