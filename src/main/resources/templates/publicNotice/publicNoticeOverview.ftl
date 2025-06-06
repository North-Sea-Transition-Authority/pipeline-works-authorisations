<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="allPublicNoticesView" type="uk.co.ogauthority.pwa.model.view.publicnotice.AllPublicNoticesView" -->
<#-- @ftlvariable name="existingPublicNoticeActions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction>" -->
<#-- @ftlvariable name="actionUrlMap" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->



<#include '../layout.ftl'>
<#include 'publicNoticeView.ftl'>

<@defaultPage htmlTitle="${appRef} public notices" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Public notices</h2>

  <#if !allPublicNoticesView.hasPublicNotices()>
    <@fdsInsetText.insetText>No public notices have been created for this application</@fdsInsetText.insetText>
  </#if>

  <#if allPublicNoticesView.currentPublicNotice?has_content>
    <@publicNoticeView publicNoticeViewData=allPublicNoticesView.currentPublicNotice existingPublicNoticeActions=existingPublicNoticeActions publicNoticeActions=allPublicNoticesView.actions/>
  </#if>

  <#if (allPublicNoticesView.historicalPublicNotices?size > 0)>
    <@fdsDetails.summaryDetails summaryTitle="Show previous public notices">
      <#assign count = allPublicNoticesView.historicalPublicNotices?size>
      <#list allPublicNoticesView.historicalPublicNotices as historicalPublicNotice>
        <@publicNoticeView publicNoticeViewData=historicalPublicNotice displayAsHistoricalRequest=true historicalRequestNumber=count/>
        <hr class="govuk-section-break govuk-section-break--m">
        <#assign count-->
      </#list>
    </@fdsDetails.summaryDetails>
  </#if>

  <#if allPublicNoticesView.actions?seq_contains("NEW_DRAFT")>
      <@fdsAction.link linkText="Draft public notice" linkUrl=springUrl(draftPublicNoticeUrl) linkClass="govuk-button"/>
  </#if>


</@defaultPage>
