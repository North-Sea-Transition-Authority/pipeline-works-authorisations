<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="consulteeGroupRequestsViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../layout.ftl'>
<#include 'consultationRequestView.ftl'>

<@defaultPage htmlTitle="${appRef} consultations" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Consultations</h2>

  <#if consulteeGroupRequestsViews?size == 0>
    <@fdsInsetText.insetText>This application has not been consulted on</@fdsInsetText.insetText>
  </#if>

  <#list consulteeGroupRequestsViews as consultationRequestViewData>

    <@consultationRequestView consultationRequestViewData=consultationRequestViewData.currentRequest>
      <#if consultationRequestViewData.currentRequest.canWithdraw>
          <@fdsAction.link linkText="Withdraw consultation" linkUrl=springUrl(consultationsUrlFactory.getWithdrawConsultationUrl(consultationRequestViewData.currentRequest.consultationRequestId))
          linkClass="govuk-link" linkScreenReaderText="Withdraw consultation" role=false start=false />
      </#if>
    </@consultationRequestView>

    <#if (consultationRequestViewData.historicalRequests)?has_content>
      <@fdsDetails.summaryDetails summaryTitle="Show previous consultations">
          <#list consultationRequestViewData.historicalRequests as consultationRequestHistoricalView>
                <@consultationRequestView consultationRequestHistoricalView/>
          </#list>
      </@fdsDetails.summaryDetails>
    </#if>

    <hr class="govuk-section-break govuk-section-break--m">

  </#list>

  <@fdsAction.link linkText="Request consultations" linkUrl=springUrl(requestConsultationsUrl) linkClass="govuk-button"/>

</@defaultPage>