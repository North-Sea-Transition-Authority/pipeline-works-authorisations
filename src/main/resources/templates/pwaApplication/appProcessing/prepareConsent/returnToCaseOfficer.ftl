<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="caseOfficerOptions" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Return to case officer" />

<@defaultPage htmlTitle=pageHeading phaseBanner=false fullWidthColumn=true breadcrumbs=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList />
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Return to case officer</h2>

  <@fdsForm.htmlForm>

      <@fdsSearchSelector.searchSelectorEnhanced path="form.caseOfficerPersonId" options=caseOfficerOptions labelText="Case officer" inputClass="govuk-!-width-two-thirds" />

      <@fdsTextarea.textarea path="form.returnReason" labelText="Enter a reason for sending back to the case officer" characterCount=true maxCharacterLength=maxCharacterLength?c inputClass="govuk-!-width-two-thirds" />

      <@fdsAction.submitButtons primaryButtonText="Return to case officer" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl) />

  </@fdsForm.htmlForm>

</@defaultPage>