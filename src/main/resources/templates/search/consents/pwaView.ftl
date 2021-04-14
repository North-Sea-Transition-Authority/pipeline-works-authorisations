<#include '../../layout.ftl'>
<#import 'consentSearchTopLevelView.ftl' as consentSearchTopLevelView>
<#import 'tabs/pipelinesTab.ftl' as pipelinesTab>
<#import 'tabs/consentHistoryTab.ftl' as consentHistoryTa>

<#-- @ftlvariable name="consentSearchResultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->
<#-- @ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.PwaViewTab>" -->
<#-- @ftlvariable name="currentProcessingTab" type="uk.co.ogauthority.pwa.service.search.consents.PwaViewTab" -->
<#-- @ftlvariable name="pwaViewUrlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory" -->
<#-- @ftlvariable name="pwaPipelineViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView>" -->
<#-- @ftlvariable name="pwaConsentHistoryViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaConsentApplicationDto>" -->



<@defaultPage htmlTitle="View PWA" fullWidthColumn=true topNavigation=true breadcrumbs=true wrapperWidth=true caption="View PWA">

    <h1 class="govuk-heading-xl">${consentSearchResultView.pwaReference!}</h1>

    <@consentSearchTopLevelView.topLevelData consentSearchResultView/>

    <@fdsBackendTabs.tabList>
        <#list availableTabs as tab>
            <@fdsBackendTabs.tab tabLabel=tab.getLabel() tabUrl=pwaViewUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value />
        </#list>
    </@fdsBackendTabs.tabList>

    <#list availableTabs as tab>

      <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=currentProcessingTab.value tabValue=tab.value>

        <#if tab == "PIPELINES">
            <@pipelinesTab.tab  urlFactory=pwaViewUrlFactory pwaPipelineViews=pwaPipelineViews/>

        <#elseif tab == "CONSENT_HISTORY">
            <@consentHistoryTa.tab  urlFactory=pwaViewUrlFactory pwaConsentHistoryViews=pwaConsentHistoryViews/>
        </#if>
    
      </@fdsBackendTabs.tabContent>

    </#list>




</@defaultPage>

