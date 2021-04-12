<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->
<#-- @ftlvariable name="formattedMedianLineDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">Varying an existing PWA where any new pipeline being installed in the Variation work scope is
      more than 500m in length and outside an HSE recognised safety zone. This requires a 28 day Public Notice.</p>

    <p class="govuk-body">A consented variation to the pipeline works authorisation should be in place before any pipeline
      or pipeline system construction works begins.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} from receipt of a satisfactory
      application to issuing the authorisation. Where there are Median Line implications it will take ${formattedMedianLineDuration}.</p>

  </@fdsStartPage.startPage>

</@defaultPage>