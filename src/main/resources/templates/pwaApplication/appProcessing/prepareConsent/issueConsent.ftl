<#include '../../../layout.ftl'>
<#import 'nonBlockingWarning.ftl' as nonBlockingWarning>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="nonBlockingTasksWarning" type=" uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.NonBlockingTasksWarning>" -->
<#-- @ftlvariable name="sosdConsultationRequestView" type="java.util.List<"uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView>" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Issue consent" />

<@defaultPage htmlTitle=pageHeading phaseBanner=false fullWidthColumn=true breadcrumbs=true>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@nonBlockingWarning.nonBlockingWarningBanner nonBlockingTasksWarning/>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Issue consent</h2>

  <@fdsWarning.warning>
    A copy of the consent will be sent to the applicant and interested third parties. The application will be completed and the official pipeline dataset will be updated.
    This action cannot be undone.
  </@fdsWarning.warning>  

  <#if sosdConsultationRequestView?has_content>      
    <@fdsCheckAnswers.checkAnswersWrapper summaryListId="" headingText="Consultation Secretary of State’s decision documents" headingSize="h3" headingClass="govuk-heading-m">
      <@fdsCheckAnswers.checkAnswers summaryListClass="">
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText=sosdConsultationRequestView.consultationResponseDocumentType.displayName>
            <@pwaFiles.uploadedFileList downloadUrl=springUrl(sosdConsultationRequestView.downloadFileUrl) existingFiles=sosdConsultationRequestView.consultationResponseFileViews blockClass="case-history" />
        </@fdsCheckAnswers.checkAnswersRowNoAction>
      </@fdsCheckAnswers.checkAnswers>
    </@fdsCheckAnswers.checkAnswersWrapper>
  </#if>

  <@fdsForm.htmlForm>
      <@fdsAction.submitButtons primaryButtonText="Issue consent" linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl) />
  </@fdsForm.htmlForm>

</@defaultPage>