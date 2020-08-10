<#include 'layout.ftl'>
<#import 'workarea/applicationsTab.ftl' as applicationsTab>
<#import 'workarea/consultationsTab.ftl' as consultationsTab>

<#-- @ftlvariable name="startPwaApplicationUrl" type="java.lang.String" -->
<#-- @ftlvariable name="prototypeApplicationUrl" type="java.lang.String" -->
<#--@ftlvariable name="workAreaResult" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaResult" -->
<#--@ftlvariable name="tabUrlFactory" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaTabUrlFactory" -->
<#--@ftlvariable name="currentWorkAreaTab" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaTab" -->
<#--@ftlvariable name="availableTabs" type="java.util.List<uk.co.ogauthority.pwa.service.workarea.WorkAreaTab>" -->

<@defaultPage htmlTitle="Work area" pageHeading="Work area" topNavigation=true fullWidthColumn=true>


    <@fdsAction.link linkText="Start PWA application" linkUrl=springUrl(startPwaApplicationUrl) linkClass="govuk-button"/>

    <@fdsAction.link linkText="Start Prototype PWA application" linkUrl=springUrl(prototypeApplicationUrl) linkClass="govuk-button"/>

    <@fdsTabs.tabs tabsHeading="Work area tabs">
        <@fdsTabs.tabList>
            <#list availableTabs as tab>
              <@fdsTabs.tab tabLabel=tab.label tabUrl=tabUrlFactory.getTabUrl(tab.value) tabAnchor=tab.anchor currentTab=currentWorkAreaTab.value tabValue=tab.value />
            </#list>
        </@fdsTabs.tabList>
        <#list availableTabs as tab>

            <@fdsTabs.tabContent tabLabel=tab.label tabAnchor=tab.anchor currentTab=currentWorkAreaTab.value tabValue=tab.value>

                <#if tab == "OPEN_APPLICATIONS">
                    <@applicationsTab.tab workAreaPageView=workAreaResult.getApplicationsTabPages()! />
                </#if>

                <#if tab == "OPEN_CONSULTATIONS">
                    <@consultationsTab.tab workAreaPageView=workAreaResult.getConsultationsTabPages()! />
                </#if>

            </@fdsTabs.tabContent>

        </#list>

    </@fdsTabs.tabs>

</@defaultPage>