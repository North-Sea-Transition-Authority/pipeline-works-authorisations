<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="parallelConsentViews" type="java.util.List<uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ParallelConsentView>" -->
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

    <@fdsForm.htmlForm>

        <#if parallelConsentViews?has_content>

            <#assign parallelConsentsReviewedMoreNestedContent>
                <table  class="govuk-table">
                    <thead class="govuk-table__head">
                        <tr class="govuk-table__row">
                            <th scope="col" class="govuk-table__header">Consent reference</th>
                            <th scope="col" class="govuk-table__header">Application reference</th>
                            <th scope="col" class="govuk-table__header">Consented date</th>
                        </tr>
                    </thead>
                    <tbody class="govuk-table__body">
                        <#list parallelConsentViews as consentView>
                        <tr class="govuk-table__row">
                            <th scope="row" class="govuk-table__header">${consentView.consentReference!""}</th>
                            <td class="govuk-table__cell">${consentView.applicationReference!""}</td>
                            <td class="govuk-table__cell">${consentView.formattedConsentDate!""}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </#assign>

            <@fdsCheckbox.checkboxGroup path="form.parallelConsentsReviewedIfApplicable"
              fieldsetHeadingText="Review consents issued after application created"
              hintText="If the consent does not accurately reflect previous changes you must return the application to the applicant for update"
              hiddenContent=false
              smallCheckboxes=true
              moreNestedContent=parallelConsentsReviewedMoreNestedContent>
                 <@fdsCheckbox.checkboxItem path="form.parallelConsentsReviewedIfApplicable" labelText="All required changes from previous consents are reflected in this consent"/>
            </@fdsCheckbox.checkboxGroup>
        </#if>

        <@fdsTextarea.textarea path="form.coverLetterText" labelText="Consent email cover letter" inputClass="govuk-!-width-full" />

        <@fdsAction.submitButtons primaryButtonText="Send for approval" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

  </div>

</@defaultPage>
