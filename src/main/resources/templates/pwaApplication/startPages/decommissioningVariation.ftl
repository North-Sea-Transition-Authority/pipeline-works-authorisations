<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">For proposed subsea pipeline works associated with Decommissioning- only to be used on the
      condition that OGA has agreed COP and the Operator has confirmed the last day of production in writing to OGA
      Operations</p>

    <p class="govuk-body">Where there are no objections, it takes approximately 6+ (note where there are Median Line implications this will take 6+ months) to authorisation</p>

  </@fdsStartPage.startPage>

</@defaultPage>