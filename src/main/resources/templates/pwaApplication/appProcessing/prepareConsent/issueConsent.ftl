<#include '../../../layout.ftl'>
<#import 'nonBlockingWarning.ftl' as nonBlockingWarning>
<#import '../../../consultation/consultationSosd.ftl' as consultationSosd>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="nonBlockingTasksWarning" type="uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.NonBlockingTasksWarning" -->
<#-- @ftlvariable name="sosdConsultationRequestView" type="java.util.List<"uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView>" -->
<#-- @ftlvariable name="pipelineTransferPageBannerView" type="uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView" -->
<#-- @ftlvariable name="consentTransferBlock" type="java.lang.Boolean" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Issue consent" />

<@defaultPage htmlTitle=pageHeading phaseBanner=false fullWidthColumn=true breadcrumbs=true>



  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@nonBlockingWarning.nonBlockingWarningBanner nonBlockingTasksWarning/>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <#if pipelineTransferPageBannerView?has_content>
      <@notificationBanner.infoNotificationBanner notificationBannerView=pipelineTransferPageBannerView />
  </#if>

  <h2 class="govuk-heading-l">Issue consent</h2>

  <@fdsWarning.warning>
    A copy of the consent will be sent to the applicant and interested third parties. The application will be completed and the official pipeline dataset will be updated.
    This action cannot be undone.
  </@fdsWarning.warning>

  <@consultationSosd.sosdFileView sosdConsultationRequestView/>

  <@fdsForm.htmlForm>
      <#if consentTransferBlock>
        <@fdsAction.link linkText="Go back" linkClass="govuk-button govuk-button--secondary" linkUrl=springUrl(cancelUrl) />
      <#else>
        <@fdsAction.submitButtons primaryButtonText="Issue consent" linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl) />
      </#if>
      </@fdsForm.htmlForm>

</@defaultPage>
