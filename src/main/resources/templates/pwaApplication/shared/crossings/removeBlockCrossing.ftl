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

    <#if crossing.blockOwnedCompletelyByHolder || crossing.blockOperatorList?has_content>
      <h2 class="govuk-heading-s">Owner</h2>
      <ul class="govuk-list">
          <#if crossing.blockOwnedCompletelyByHolder>
            <li>Holder owned</li>
          </#if>
          <#list crossing.blockOperatorList as operator>
            <li>${operator}</li>
          </#list>
      </ul>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove block crossing" secondaryLinkText="Back to licence and blocks" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>