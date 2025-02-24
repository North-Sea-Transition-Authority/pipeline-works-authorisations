<#-- @ftlvariable name="teamTypeViews" type="java.util.List<uk.co.ogauthority.pwa.teams.management.view.TeamTypeView>" -->
<#include '../layout.ftl'>

<#assign pageTitle="Select a team"/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle

twoThirdsColumn=true
>

    <@fdsResultList.resultList>
        <#list teamTypeViews as teamTypeView>
            <@fdsResultList.resultListItem
            linkHeadingUrl=springUrl(teamTypeView.manageUrl())
            linkHeadingText=teamTypeView.teamTypeName()/>
        </#list>
    </@fdsResultList.resultList>

</@defaultPage>