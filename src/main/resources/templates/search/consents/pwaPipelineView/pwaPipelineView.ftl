<#include '../../../layout.ftl'>
<#import '../consentSearchTopLevelView.ftl' as consentSearchTopLevelView>
<#import 'tabs/pipelineHistoryTab.ftl' as pipelineHistoryTab>
<#import 'tabs/huooHistoryTab.ftl' as huooHistoryTab>
<#import 'tabs/asBuiltSubmissionHistoryTab.ftl' as asBuiltSubmissionHistoryTab>


<#-- @ftlvariable name="consentSearchResultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->
<#-- @ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.PwaViewTab>" -->
<#-- @ftlvariable name="currentProcessingTab" type="uk.co.ogauthority.pwa.service.search.consents.PwaViewTab" -->
<#-- @ftlvariable name="pwaPipelineViewUrlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaPipelineViewUrlFactory" -->
<#-- @ftlvariable name="diffedPipelineSummaryModel" type="java.util.Map<java.lang.String, Object>" -->
<#-- @ftlvariable name="diffedHuooSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups" -->
<#-- @ftlvariable name="showPwaNavigation" type="java.lang.Boolean" -->




<@defaultPage htmlTitle="View PWA pipeline ${pipelineReference}" fullWidthColumn=true topNavigation=showPwaNavigation breadcrumbs=showPwaNavigation wrapperWidth=true caption="View pipeline (PWA - ${consentSearchResultView.pwaReference})">

    <h1 class="govuk-heading-xl">${pipelineReference}</h1>

    <@consentSearchTopLevelView.topLevelData consentSearchResultView/>

    <@fdsBackendTabs.tabList>
        <#list availableTabs as tab>
            <@fdsBackendTabs.tab tabLabel=tab.getLabel() tabUrl=pwaPipelineViewUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value />
        </#list>
    </@fdsBackendTabs.tabList>


   <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value>

      <#if currentProcessingTab == "PIPELINE_HISTORY">
          <@pipelineHistoryTab.tab diffedPipelineSummaryModel/>
      <#elseif currentProcessingTab == "HUOO_HISTORY">
          <@huooHistoryTab.tab diffedHuooSummary/>
      <#elseif currentProcessingTab == "AS_BUILT_NOTIFICATION_HISTORY">
          <@asBuiltSubmissionHistoryTab.tab submissionHistoryView isOgaUser/>
      </#if>

    </@fdsBackendTabs.tabContent>




</@defaultPage>

