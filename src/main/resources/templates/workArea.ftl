<#include 'layout.ftl'>

<#-- @ftlvariable name="startPwaApplicationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Work area" pageHeading="Work area" twoThirdsColumn=false>

    <@fdsAction.link linkText="Start new PWA" linkUrl=springUrl(startPwaApplicationUrl) linkClass="govuk-button"/>

</@defaultPage>