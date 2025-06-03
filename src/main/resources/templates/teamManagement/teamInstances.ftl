<#-- @ftlvariable name="teamViews" type="java.util.List<uk.co.ogauthority.pwa.teams.management.view.TeamView>" -->
<#include '../layout.ftl'>

<#assign pageTitle="Select a team"/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
topNavigation=true
twoThirdsColumn=true
>
    <#if createNewInstanceUrl?has_content>
        <@fdsAction.link linkText="Create team" linkUrl=springUrl(createNewInstanceUrl) linkClass="govuk-button"/>
    </#if>

    <@fdsResultList.resultList resultCount=teamViews?size resultCountSuffix="team">
        <#list teamViews as teamView>
            <@fdsResultList.resultListItem
            linkHeadingUrl=springUrl(teamView.manageUrl())
            linkHeadingText=teamView.teamName()/>
        </#list>
    </@fdsResultList.resultList>

</@defaultPage>