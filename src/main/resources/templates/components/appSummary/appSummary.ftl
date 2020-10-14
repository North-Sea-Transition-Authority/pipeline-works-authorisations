<#include '../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->

<#macro summary pageHeading appSummaryView sidebarHeading caseSummaryView=[]>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>

            <@fdsSubNavigation.subNavigationSection themeHeading=sidebarHeading>

                <#list appSummaryView.sidebarSectionLinks as sidebarLink>
                    <@pwaSidebarSectionLink.renderSidebarLink sidebarLink=sidebarLink/>
                </#list>

            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent pageHeading=pageHeading>

        <#if caseSummaryView?has_content>
            <@pwaCaseSummary.summary caseSummaryView=caseSummaryView showAppSummaryLink=false />
        </#if>

        <@diffChanges.toggler togglerLabel="Show differences from consented data"/>
        ${appSummaryView.summaryHtml?no_esc}

        <#nested>

    </@defaultPagePaneContent>

</#macro>