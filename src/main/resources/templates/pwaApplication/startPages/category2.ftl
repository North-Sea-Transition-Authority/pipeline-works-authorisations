<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->
<#-- @ftlvariable name="formattedMedianLineDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">A category 2 variation is used when varying an existing PWA for the following reasons:</p>

    <ul class="govuk-list govuk-list--bullet">
      <li>adding new pipelines less than 500m in length or totally within a HSE recognised safety zone</li>
      <li>varying an existing pipeline in the PWA Regime</li>
      <li>partially or fully removing an existing pipeline within the PWA Regime from the seabed or taking it out of use
        prior to agreement of Cessation of Production approval</li>
      <li>bringing a pipeline into the PWA Regime that already exists on the seabed</li>
    </ul>

    <p class="govuk-body">A consented variation to the pipeline works authorisation should be in place before any pipeline
      or pipeline system construction works begins.</p>

    <p class="govuk-body">Where there are no objections, it takes ${formattedDuration} from receipt of a satisfactory application to
      issuing the authorisation. Where there are Median Line implications it will take ${formattedMedianLineDuration}.</p>

  </@fdsStartPage.startPage>

</@defaultPage>