<#-- @ftlvariable name="team" type="uk.co.ogauthority.pwa.model.teams.PwaTeam" -->
<#-- @ftlvariable name="teamMember" type="uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Remove User" pageHeading="Are you sure you want to remove this user from the ${team.name} team?" backLink=true topNavigation=true twoThirdsColumn=false>

    <#if error?has_content>
        <@fdsError.singleErrorSummary errorMessage=error/>
    </#if>

    <@fdsForm.htmlForm>
      <table class="govuk-table">
        <caption class="govuk-table__caption govuk-visually-hidden">Are you sure you want to remove this user from ${team.name}?</caption>
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Full name</th>
          <td class="govuk-table__cell">
              ${teamMember.getFullName()}
          </td>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Email address</th>
          <td class="govuk-table__cell">
              <#if teamMember.emailAddress?has_content>
                  ${teamMember.emailAddress}
              </#if>
          </td>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Roles</th>
          <td class="govuk-table__cell">
              <#list teamMember.roleViews?sort_by("displaySequence") as role>
                  ${role.title}<#if role_has_next>,</#if>
              </#list>
          </td>
        </tr>
        </tbody>
      </table>
        <@fdsAction.submitButtons primaryButtonText="Remove" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>