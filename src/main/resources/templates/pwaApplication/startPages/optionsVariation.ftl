<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <@fdsInsetText.insetText>
      The Holder should detail why they would like OGA to consider the case to be handled as an Options.
    </@fdsInsetText.insetText>

    <p class="govuk-body">Where the problem with a pipeline(s) may not be clearly identified and there may be various
      points of possible failure, OGA may consider an Options case.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately 6-8 weeks to authorisation.</p>

  </@fdsStartPage.startPage>

</@defaultPage>