<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="currentProcessingTab" type="uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab" -->
<#-- @ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab>" -->
<#-- @ftlvariable name="tabUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTabUrlFactory" -->
<#-- @ftlvariable name="taskListGroups" type="java.util.List<uk.co.ogauthority.pwa.model.tasklist.TaskListGroup>" -->
<#-- @ftlvariable name="caseHistoryItems" type="java.util.List<uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView>" -->
<#-- @ftlvariable name="industryFlag" type="java.lang.Boolean" -->
<#-- @ftlvariable name="updateRequestView" type="uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="processingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission> -->

<#include '../../layout.ftl'>
<#import 'tabs/tasksTab.ftl' as tasksTab>
<#import 'tabs/caseHistoryTab.ftl' as caseHistoryTab>
<#import 'tabs/firsTab.ftl' as firsTab>

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} case management" topNavigation=true fullWidthColumn=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <#if updateRequestView?has_content>
      <@pwaUpdateRequestView.banner view=updateRequestView canUpdate=processingPermissions?seq_contains("UPDATE_APPLICATION") taskListUrl=taskListUrl />
  </#if>

  <@fdsBackendTabs.tabList>
      <#list availableTabs as tab>
          <@fdsBackendTabs.tab tabLabel=tab.getLabel(industryFlag) tabUrl=tabUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value />
      </#list>
  </@fdsBackendTabs.tabList>

  <#list availableTabs as tab>

      <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value>

          <#if tab == "TASKS">
              <@tasksTab.tab taskListGroups=taskListGroups industryFlag=industryFlag />
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