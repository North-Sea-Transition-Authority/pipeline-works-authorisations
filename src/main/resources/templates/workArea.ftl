<#include 'layout.ftl'>
<#import 'workarea/applicationsTab.ftl' as applicationsTab>
<#import 'workarea/consultationsTab.ftl' as consultationsTab>

<#-- @ftlvariable name="startPwaApplicationUrl" type="java.lang.String" -->
<#--@ftlvariable name="workAreaResult" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaResult" -->
<#--@ftlvariable name="tabUrlFactory" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaTabUrlFactory" -->
<#--@ftlvariable name="currentWorkAreaTab" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaTab" -->
<#--@ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.workarea.WorkAreaTab>" -->

<@defaultPage htmlTitle="Work area" pageHeading="Work area" topNavigation=true fullWidthColumn=true>


    <@fdsAction.link linkText="Start PWA application" linkUrl=springUrl(startPwaApplicationUrl) linkClass="govuk-button" role=true/>

    <@fdsBackendTabs.tabs tabsHeading="Work area tabs">
        <@fdsBackendTabs.tabList>
            <#list availableTabs as tab>
              <@fdsBackendTabs.tab tabLabel=tab.label tabUrl=tabUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentWorkAreaTab.value tabValue=tab.value />
            </#list>
        </@fdsBackendTabs.tabList>
        <#list availableTabs as tab>

            <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=currentWorkAreaTab.value tabValue=tab.value>

                <#if tab == "OPEN_APPLICATIONS">
                    <@applicationsTab.tab workAreaPageView=workAreaResult.getApplicationsTabPages()! />
                </#if>

                <#if tab == "OPEN_CONSULTATIONS">
                    <@consultationsTab.tab workAreaPageView=workAreaResult.getConsultationsTabPages()! />
                </#if>

            </@fdsBackendTabs.tabContent>

        </#list>

    </@fdsBackendTabs.tabs>

</@defaultPage>