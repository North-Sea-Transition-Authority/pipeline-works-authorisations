<#include '../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->

<#macro summary pageHeading appSummaryView sidebarHeading caseSummaryView=[] errorList="" aboveSummaryInsert="">

    <@_summarySideNav appSummaryView=appSummaryView sidebarHeading=sidebarHeading></@_summarySideNav>

    <@_summaryMainContent pageHeading=pageHeading appSummaryView=appSummaryView caseSummaryView=caseSummaryView errorList=errorList aboveSummaryInsert=aboveSummaryInsert>
        <#nested>
    </@_summaryMainContent>

</#macro>


<#macro _summarySideNav appSummaryView sidebarHeading>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>

            <@fdsSubNavigation.subNavigationSection themeHeading=sidebarHeading>

                <#list appSummaryView.sidebarSectionLinks as sidebarLink>
                    <@pwaSidebarSectionLink.renderSidebarLink sidebarLink=sidebarLink/>
                </#list>

            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

</#macro>


<#macro _summaryMainContent pageHeading appSummaryView caseSummaryView=[] errorList="" aboveSummaryInsert="">

    <@defaultPagePaneContent pageHeading=pageHeading>

        <#if errorList?has_content>
            <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
        </#if>

        ${aboveSummaryInsert!}

        <#if caseSummaryView?has_content>
            <@pwaCaseSummary.summary caseSummaryView=caseSummaryView showAppSummaryLink=false />
        </#if>

        <@diffChanges.toggler togglerLabel="Show differences from consented data"/>
        ${appSummaryView.summaryHtml?no_esc}

        <#nested>

    </@defaultPagePaneContent>

</#macro>