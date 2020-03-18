<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">Varying an existing PWA and any new pipeline being installed in the Variation work scope is more than 500m in length and outside an HSE recognised safety zone. This also requires a 28 day Public Notice.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately 4-6 months (note where there are Median Line implications this will take 6+ months) to authorisation</p>

  </@fdsStartPage.startPage>

</@defaultPage>