<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="consulteeGroupRequestsViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="requestConsultationsUrl" type="String" -->
<#-- @ftlvariable name="canEditConsultations" type="java.lang.Boolean" -->

<#include '../layout.ftl'>
<#include 'consultationRequestView.ftl'>

<@defaultPage htmlTitle="${appRef} consultations" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Consultations</h2>

  <#if consulteeGroupRequestsViews?size == 0>
    <@fdsInsetText.insetText>This application has not been consulted on</@fdsInsetText.insetText>
  </#if>

  <#list consulteeGroupRequestsViews as consultationRequestViewData>

    <@consultationRequestView consultationRequestViewData=consultationRequestViewData.currentRequest applicationReference=caseSummaryView.pwaApplicationRef>
      <#if consultationRequestViewData.currentRequest.canWithdraw>
          <@fdsAction.link linkText="Withdraw consultation" linkUrl=springUrl(consultationsUrlFactory.getWithdrawConsultationUrl(consultationRequestViewData.currentRequest.consultationRequestId))
          linkClass="govuk-link" linkScreenReaderText="for ${consultationRequestViewData.currentRequest.consulteeGroupName}" role=false start=false />
      </#if>
    </@consultationRequestView>

    <#if (consultationRequestViewData.historicalRequests)?has_content>
      <#assign screenreaderTitle>
        <span class="govuk-visually-hidden">for ${consultationRequestViewData.currentRequest.consulteeGroupName}</span>
      </#assign>
      <@fdsDetails.summaryDetails summaryTitle="Show previous consultations ${screenreaderTitle}"> 
          <#list consultationRequestViewData.historicalRequests as consultationRequestHistoricalView>
                <@consultationRequestView consultationRequestViewData=consultationRequestHistoricalView applicationReference=caseSummaryView.pwaApplicationRef displayAsHistoricalRequest=true/>
          </#list>
      </@fdsDetails.summaryDetails>
    </#if>

    <hr class="govuk-section-break govuk-section-break--m">

  </#list>

  <#if canEditConsultations>
      <@fdsAction.link linkText="Request consultations" linkUrl=springUrl(requestConsultationsUrl) linkClass="govuk-button"/>
  </#if>

</@defaultPage>