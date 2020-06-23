<#include '../layout.ftl'>

<@defaultPage htmlTitle="Select Team" pageHeading="Select a team" topNavigation=true twoThirdsColumn=false backLink=true>
  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Name</th>
      <th class="govuk-table__header" scope="col">Description</th>
      <th class="govuk-table__header" scope="col">Actions</th>
    </tr>
    <tbody class="govuk-table__body">
    <#list teamViewList as team>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
            ${team.name}
        </td>
        <td class="govuk-table__cell">
            ${team.description?has_content?then(team.description, "")}
        </td>
        <td class="govuk-table__cell">
          <a class="govuk-link" href="${springUrl(team.getSelectRoute())}">Manage team</a>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>
</@defaultPage>