<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Send consent for approval" />

<@defaultPage htmlTitle=pageHeading phaseBanner=false fullWidthColumn=true breadcrumbs=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList />
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Send consent for approval</h2>

  <div class="govuk-!-width-two-thirds">
    <@fdsWarning.warning>
      You will not be able to make any updates to the consent or other tasks for this application while the consent is being reviewed.
    </@fdsWarning.warning>
  </div>

  <@fdsForm.htmlForm>

      <@fdsTextarea.textarea path="form.coverLetterText" labelText="Consent email cover letter" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-full" />

      <@fdsAction.submitButtons primaryButtonText="Send for approval" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>

</@defaultPage>