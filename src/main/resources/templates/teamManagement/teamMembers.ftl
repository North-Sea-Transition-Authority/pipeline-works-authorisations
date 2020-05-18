<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamMemberViews" type="java.util.List<uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView>" -->
<#-- @ftlvariable name="addUserUrl" type="java.lang.String" -->
<#-- @ftlvariable name="showBreadcrumbs" type="java.lang.Boolean" -->
<#-- @ftlvariable name="userCanManageAccess" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->
<#-- @ftlvariable name="allRoles" type="java.util.Map<String,String>" -->

<#include "../layout.ftl">

<@defaultPage htmlTitle=teamName backLink=!showBreadcrumbs pageHeading=teamName topNavigation=showTopNav twoThirdsColumn=false breadcrumbs=showBreadcrumbs>

    <#if allRoles??>
      <@fdsDetails.summaryDetails summaryTitle="What can each role do?" >
        <table class="govuk-table">
            <tbody class="govuk-table__body">
            <#list allRoles as propName, propValue>
              <#assign name = propName?lower_case?cap_first?replace("_"," ") >
              <#assign description = propValue?keep_before("(") >
                  <tr class="govuk-table__row">
                      <td class="govuk-table__cell"> ${name} </td>
                      <td class="govuk-table__cell"> ${description} </td>
                  </tr>
            </#list>
            </tbody>
        </table>
      </@fdsDetails.summaryDetails>
    </#if>


    <#if userCanManageAccess>
        <@fdsAction.link linkText="Add user" linkUrl=springUrl(addUserUrl) linkClass="govuk-button govuk-button--blue" role=true/>
    </#if>

    <#list teamMemberViews>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Name</th>
          <th class="govuk-table__header" scope="col">Contact details</th>
          <th class="govuk-table__header" scope="col">Roles</th>
          <#if userCanManageAccess>
            <th class="govuk-table__header" scope="col">Actions</th>
          </#if>
        </tr>
        <tbody class="govuk-table__body">
        <#items as teamMemberView>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">
                ${teamMemberView.fullName}
            </td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                <li>${teamMemberView.emailAddress}</li>
                <li>${teamMemberView.telephoneNo}</li>
              </ul>
            </td>
            <td class="govuk-table__cell">
                <#list teamMemberView.roleViews?sort_by("displaySequence") as roleView>
                    ${roleView.title}
                  <br>
                </#list>
            </td>
            <#if userCanManageAccess>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <li>
                      <@fdsAction.link linkText="Edit" linkUrl=springUrl(teamMemberView.editRoute) linkClass="govuk-link"></@fdsAction.link>
                  </li>
                  <li>
                      <@fdsAction.link linkText="Remove" linkUrl=springUrl(teamMemberView.removeRoute) linkClass="govuk-link"></@fdsAction.link>
                  </li>
                </ul>
              </td>
            </#if>
          </tr>
        </#items>
        </tbody>
      </table>
    </#list>

    <#if backUrl??>
      <@fdsAction.link linkText="Complete section" linkClass="govuk-button"  linkUrl=springUrl(backUrl)/>
    </#if>

</@defaultPage>