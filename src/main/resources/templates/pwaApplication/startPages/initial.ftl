<#-- @ftlvariable name="startUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->
<#-- @ftlvariable name="resourceTypeGuideText" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start new PWA" pageHeading="Start new PWA" backLink=true>

  <@fdsStartPage.startPage startActionText="Start" startActionUrl=startUrl>

    <p class="govuk-body">${resourceTypeGuideText}</p>

    <p class="govuk-body">A consented pipeline works authorisation should be in place before any pipeline or pipeline system
      construction works begins.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} from receipt of a satisfactory
      application to issuing the authorisation. Where there are Median Line implications it will take ${formattedMedianLineDuration}.</p>

  </@fdsStartPage.startPage>

</@defaultPage>