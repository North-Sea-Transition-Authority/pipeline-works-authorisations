<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="combinedSummaryHtml" type="java.lang.String" -->
<#-- @ftlvariable name="sidebarSectionLinks" type="java.util.List<uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink>" -->
<#-- @ftlvariable name="sidebarLink" type="uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->

<#assign pageHeading="Review and Submit Application ${applicationReference}"/>

<#macro renderSidebarLink sidebarLink>
    <#local linkUrl = sidebarLink.isAnchorLink?then(sidebarLink.link, springUrl(sidebarLink.link) )>

    <@fdsSubNavigation.subNavigationSectionItem
    linkName=sidebarLink.displayText
    currentItemHref="#top"
    linkAction=linkUrl
    />

</#macro>
<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>
            <@fdsSubNavigation.subNavigationSection themeHeading="Check your answers for all questions in the application">

                <#list sidebarSectionLinks as sidebarLink>
                  <@renderSidebarLink sidebarLink=sidebarLink/>

                </#list>

            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent pageHeading=pageHeading>
        <@diffChanges.toggler togglerLabel="Show differences from consented data"/>
        ${combinedSummaryHtml?no_esc}

        <@fdsForm.htmlForm>
            <!-- Submit button macro not used to allow for hiding of button when application is not valid. -->
            <@fdsAction.button buttonText="Submit" buttonValue="submit" />
            <@fdsAction.link linkText="Back to task list" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(taskListUrl)/>
        </@fdsForm.htmlForm>
    </@defaultPagePaneContent>

</@defaultPagePane>