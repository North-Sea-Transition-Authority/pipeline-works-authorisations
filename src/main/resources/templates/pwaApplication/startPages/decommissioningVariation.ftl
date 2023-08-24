<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->
<#-- @ftlvariable name="formattedMedianLineDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">For proposed subsea pipeline works associated with Decommissioning.
      This is only to be used on the condition that the operator has confirmed the last day of production in writing to NSTA Operations.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} from receipt of a satisfactory
      application to issuing the authorisation. Where there are Median Line implications it will take ${formattedMedianLineDuration}.</p>

  </@fdsStartPage.startPage>

</@defaultPage>