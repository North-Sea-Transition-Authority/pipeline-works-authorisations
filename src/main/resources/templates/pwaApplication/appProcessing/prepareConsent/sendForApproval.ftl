<#include '../../../layout.ftl'>
<#import 'nonBlockingWarning.ftl' as nonBlockingWarning>


<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="parallelConsentViews" type="java.util.List<uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.ParallelConsentView>" -->
<#-- @ftlvariable name="nonBlockingTasksWarning" type="uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.NonBlockingTasksWarning" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.SendForApprovalUrlFactory" -->
<#-- @ftlvariable name="markdownPreviewHtml" type="String" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Send consent for approval" />

<@defaultPage htmlTitle=pageHeading phaseBanner=false fullWidthColumn=true breadcrumbs=true>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@nonBlockingWarning.nonBlockingWarningBanner nonBlockingTasksWarning/>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList />
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Send consent for approval</h2>

  <div class="govuk-!-width-full">
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
                            <td class="govuk-table__cell">${consentView.consentReference!""}</td>
                            <td class="govuk-table__cell"><@fdsAction.link linkText=consentView.applicationReference!"" linkUrl=springUrl(urlFactory.getAppSummaryUrlByAppId(consentView.pwaApplicationId)) linkScreenReaderText="View ${consentView.applicationReference} in new tab" openInNewTab=true /></td>
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

        <@pwaMarkdownInsetText.text widgetDisplayName="cover letter text area" govNotifyMarkdown=true/>

        <@fdsTextarea.textarea path="form.coverLetterText" labelText="Consent email cover letter" inputClass="govuk-!-width-two-thirds" rows="15"/>

        <@fdsDetails.summaryDetails summaryTitle="What does '??' mean?">
          <@pwaMailMerge.manualMergeGuidance showHeading=false/>
        </@fdsDetails.summaryDetails>

        <@fdsAction.button buttonText="Preview text" buttonName="preview-text-button" buttonClass="govuk-button govuk-button--secondary"/>

        <#if markdownPreviewHtml?has_content>
          <div class="pwa-markdown__preview">
              <#noautoesc>
                  ${markdownPreviewHtml}
              </#noautoesc>
          </div>
        </#if>

        <@fdsAction.submitButtons primaryButtonText="Send for approval" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

  </div>

</@defaultPage>
