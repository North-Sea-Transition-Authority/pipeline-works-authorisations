<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="currentProcessingTab" type="uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTab" -->
<#-- @ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTab>" -->
<#-- @ftlvariable name="tabUrlFactory" type="uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTabUrlFactory" -->
<#-- @ftlvariable name="taskListGroups" type="java.util.List<uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup>" -->
<#-- @ftlvariable name="caseHistoryItems" type="java.util.List<uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView>" -->
<#-- @ftlvariable name="industryFlag" type="java.lang.Boolean" -->
<#-- @ftlvariable name="updateRequestView" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="optionsApprovalPageBanner" type="uk.co.ogauthority.pwa.model.view.banner.PageBannerView" -->
<#-- @ftlvariable name="publicNoticePageBannerView" type="uk.co.ogauthority.pwa.model.view.banner.PageBannerView" -->
<#-- @ftlvariable name="payForAppUrl" type="java.lang.String" -->
<#-- @ftlvariable name="manageAppContactsUrl" type="java.lang.String" -->
<#-- @ftlvariable name="viewAppPaymentUrl" type="java.lang.String" -->

<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="processingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission> -->
<#-- @ftlvariable name="taskGroupNameWarningMessageMap" type="java.util.Map<java.lang.String, java.lang.String>" -->

<#include '../../layout.ftl'>
<#import 'tabs/tasksTab.ftl' as tasksTab>
<#import 'tabs/caseHistoryTab.ftl' as caseHistoryTab>
<#import 'tabs/firsTab.ftl' as firsTab>

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} case management" topNavigation=true fullWidthColumn=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <#if updateRequestView?has_content>
      <@pwaUpdateRequestView.banner view=updateRequestView canUpdate=processingPermissions?seq_contains("UPDATE_APPLICATION") taskListUrl=taskListUrl />
  </#if>

  <#if optionsApprovalPageBanner?has_content>
      <@pageBanner.banner view=optionsApprovalPageBanner showBannerLinks=processingPermissions?seq_contains("UPDATE_APPLICATION") />
  </#if>

  <#if publicNoticePageBannerView?has_content>
      <@pageBanner.banner view=publicNoticePageBannerView showBannerLinks=processingPermissions?seq_contains("UPDATE_PUBLIC_NOTICE_DOC") />
  </#if>

  <#if payForAppUrl?has_content>
      <@fdsAction.link linkText="Pay for application" linkUrl=springUrl(payForAppUrl) linkClass="govuk-button govuk-button--blue" role=true/>
  </#if>

  <#if manageAppContactsUrl?has_content>
      <@fdsAction.link linkText="Manage application users" linkUrl=springUrl(manageAppContactsUrl) linkClass="govuk-button govuk-button--blue" role=true/>
  </#if>

  <#if viewPublicNoticeUrl?has_content>
      <@fdsAction.link linkText="View public notice" linkUrl=springUrl(viewPublicNoticeUrl) linkClass="govuk-button govuk-button--blue" role=true/>
  </#if>

  <#if consentHistoryUrl?has_content>
      <@fdsAction.link linkText="View consent" linkUrl=springUrl(consentHistoryUrl) linkClass="govuk-button govuk-button--blue" role=true openInNewTab=true/>
  </#if>

  <#if viewAppPaymentUrl?has_content>
      <@fdsAction.link linkText="View payment information" linkUrl=springUrl(viewAppPaymentUrl) linkClass="govuk-button govuk-button--blue" role=true/>
  </#if>

  <#if reopenAsBuiltGroupUrl?has_content>
      <@fdsAction.link linkText="Reopen as-built notification group" linkUrl=springUrl(reopenAsBuiltGroupUrl) linkClass="govuk-button govuk-button--blue" role=true/>
  </#if>

  <@fdsBackendTabs.tabList>
      <#list availableTabs as tab>
          <@fdsBackendTabs.tab tabLabel=tab.getLabel(industryFlag) tabUrl=tabUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value />
      </#list>
  </@fdsBackendTabs.tabList>

  <#list availableTabs as tab>

      <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value>

        <#if tab == "TASKS">
            <@tasksTab.tab taskListGroups=taskListGroups industryFlag=industryFlag taskGroupNameWarningMessageMap=taskGroupNameWarningMessageMap/>
        </#if>

        <#if tab == "CASE_HISTORY">
            <@caseHistoryTab.tab caseHistoryItems=caseHistoryItems />
        </#if>

        <#if tab == "FIRS">
            <@firsTab.tab />
        </#if>

      </@fdsBackendTabs.tabContent>

  </#list>

</@defaultPage>