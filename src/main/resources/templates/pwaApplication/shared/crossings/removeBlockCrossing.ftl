<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="Remove block crossing" pageHeading="Are you sure you want to remove this block crossing?" breadcrumbs=true>
    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsDataItems.dataItem>
      <@fdsDataItems.dataValues key="UK block reference" value=crossing.blockReference/>
      <@fdsDataItems.dataValues key="Licence" value=crossing.licenceReference/>
    </@fdsDataItems.dataItem>

    <h2 class="govuk-heading-s">Holders</h2>
  <ul class="govuk-list">
      <#if crossing.blockOwnedCompletelyByHolder>
        <li>Holder owned</li>
      </#if>
      <#list crossing.blockOperatorList as operator>
        <li>${operator}</li>
      </#list>
  </ul>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove block crossing" secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>