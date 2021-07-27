<#include '../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="appDetailVersionSearchSelectorItems" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="viewAppSummaryUrl" type="java.lang.String" -->
<#-- @ftlvariable name="showDiffCheckbox" type="java.lang.Boolean" -->



<#macro summary pageHeading appSummaryView sidebarHeading caseSummaryView=[] errorList="" aboveSummaryInsert="" singleErrorMessage="">

    <@_summarySideNav appSummaryView=appSummaryView sidebarHeading=sidebarHeading></@_summarySideNav>

    <@_summaryMainContent pageHeading=pageHeading appSummaryView=appSummaryView caseSummaryView=caseSummaryView errorList=errorList aboveSummaryInsert=aboveSummaryInsert singleErrorMessage=singleErrorMessage>
        <#nested>
    </@_summaryMainContent>

</#macro>


<#macro _summarySideNav appSummaryView sidebarHeading>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation sticky=true>

            <@fdsSubNavigation.subNavigationSection themeHeading=sidebarHeading>

                <#list appSummaryView.sidebarSectionLinks as sidebarLink>
                    <@pwaSidebarSectionLink.renderSidebarLink sidebarLink=sidebarLink/>
                </#list>

            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

</#macro>


<#macro _summaryMainContent pageHeading appSummaryView caseSummaryView=[] errorList="" aboveSummaryInsert="" singleErrorMessage="">

    <@defaultPagePaneContent pageHeading=pageHeading errorItems=errorList singleErrorMessage=singleErrorMessage>

        <#if caseSummaryView?has_content>
            <@pwaCaseSummary.summary caseSummaryView=caseSummaryView showAppSummaryLink=false />
        </#if>

        ${aboveSummaryInsert!}

        <#if showVersionSelector?has_content && showVersionSelector>
            <@fdsForm.htmlForm actionUrl=springUrl(viewAppSummaryUrl)>
                <@fdsSearchSelector.searchSelectorEnhanced path="form.applicationDetailId" options=appDetailVersionSearchSelectorItems labelText="Select version" />
                <@fdsAction.button buttonText="Show version"/>
            </@fdsForm.htmlForm>
        </#if>

        <@diffChanges.toggler togglerLabel="Show differences from consented data" showTogglerCheckBox=showDiffCheckbox/>

        ${appSummaryView.summaryHtml?no_esc}

        <#nested>

    </@defaultPagePaneContent>

</#macro>