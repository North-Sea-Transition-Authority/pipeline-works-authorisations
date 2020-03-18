<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamMemberViews" type="java.util.List<uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView>" -->
<#-- @ftlvariable name="addUserUrl" type="java.lang.String" -->
<#-- @ftlvariable name="showBreadcrumbs" type="java.lang.Boolean" -->

<#include "../layout.ftl">

<@defaultPage htmlTitle=teamName backLink=!showBreadcrumbs pageHeading=teamName topNavigation=true twoThirdsColumn=false breadcrumbs=showBreadcrumbs>

    <#list teamMemberViews>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Name</th>
          <th class="govuk-table__header" scope="col">Contact details</th>
          <th class="govuk-table__header" scope="col">Roles</th>
          <th class="govuk-table__header" scope="col">Actions</th>
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
          </tr>
        </#items>
        </tbody>
      </table>
    </#list>

    <@fdsAction.link linkText="Add user" linkUrl=springUrl(addUserUrl) linkClass="govuk-button" role=true/>

</@defaultPage>