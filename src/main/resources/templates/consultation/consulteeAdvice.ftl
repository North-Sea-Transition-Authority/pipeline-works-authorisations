<#-- @ftlvariable name="consulteeAdviceView" type="uk.co.ogauthority.pwa.model.form.consultation.ConsulteeAdviceView" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../layout.ftl'>
<#include 'consultationRequestView.ftl'>

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} consultations" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <span class="govuk-caption-m">${consulteeAdviceView.consulteeGroupName}</span>
  <h2 class="govuk-heading-l">Consultations</h2>

  <#if consulteeAdviceView.activeRequestView?has_content>
    <@consultationRequestView consultationRequestViewData=consulteeAdviceView.activeRequestView applicationReference=caseSummaryView.pwaApplicationRef displayAsHistoricalRequest=true/>
    <hr class="govuk-section-break govuk-section-break--m">
  </#if>

  <#list consulteeAdviceView.historicRequestViews as historicRequestView>

    <@consultationRequestView consultationRequestViewData=historicRequestView applicationReference=caseSummaryView.pwaApplicationRef displayAsHistoricalRequest=true/>

    <hr class="govuk-section-break govuk-section-break--m">

  </#list>

</@defaultPage>