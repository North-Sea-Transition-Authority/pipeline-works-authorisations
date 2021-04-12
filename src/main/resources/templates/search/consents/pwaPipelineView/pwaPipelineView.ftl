<#include '../../../layout.ftl'>
<#import '../consentSearchTopLevelView.ftl' as consentSearchTopLevelView>
<#import 'tabs/pipelineHistoryTab.ftl' as pipelineHistoryTab>
<#import 'tabs/huooHistoryTab.ftl' as huooHistoryTab>


<#-- @ftlvariable name="consentSearchResultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->
<#-- @ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.PwaViewTab>" -->
<#-- @ftlvariable name="currentProcessingTab" type="uk.co.ogauthority.pwa.service.search.consents.PwaViewTab" -->
<#-- @ftlvariable name="pwaPipelineViewUrlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaPipelineViewUrlFactory" -->
<#-- @ftlvariable name="diffedPipelineSummaryModel" type="java.util.Map<java.lang.String, Object>" -->
<#-- @ftlvariable name="diffedHuooSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups" -->




<@defaultPage htmlTitle="View PWA pipeline ${pipelineReference}" fullWidthColumn=true topNavigation=true breadcrumbs=true wrapperWidth=true caption="View pipeline (PWA - ${consentSearchResultView.pwaReference})">

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
      <#else>
          <@huooHistoryTab.tab diffedHuooSummary/>
      </#if>

    </@fdsBackendTabs.tabContent>




</@defaultPage>

