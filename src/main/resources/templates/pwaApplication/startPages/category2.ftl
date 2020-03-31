<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->
<#-- @ftlvariable name="formattedMedianLineDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <ul class="govuk-list govuk-list--bullet">
      <li>
        <p class="govuk-body">Varying an existing PWA and any new pipeline being installed in the Variation work scope is more
          than 500m in length and outside an HSE recognised safety zone. This also requires a 28 day Public Notice.</p>
      </li>
      <li>
        <p class="govuk-body">Where an existing pipeline within the PWA Regime is to be partially or fully removed from the
          seabed or taken out of use. This is prior to agreement of COP approval.</p>
      </li>
    </ul>
    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} (note where there are Median
      Line implications this will take ${formattedMedianLineDuration}) to authorisation</p>

  </@fdsStartPage.startPage>

</@defaultPage>