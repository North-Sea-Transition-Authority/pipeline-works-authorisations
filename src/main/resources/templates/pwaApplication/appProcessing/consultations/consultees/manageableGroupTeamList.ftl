<#-- @ftlvariable name="consulteeGroupViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView>" -->

<#include '../../../../layout.ftl'>

<@defaultPage htmlTitle="Select Group" pageHeading="Select a consultee group" topNavigation=true twoThirdsColumn=false backLink=true>
  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Name</th>
      <th class="govuk-table__header" scope="col">Actions</th>
    </tr>
    <tbody class="govuk-table__body">
    <#list consulteeGroupViews as group>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
            ${group.name}
        </td>
        <td class="govuk-table__cell">
          <@fdsAction.link
            linkUrl=springUrl(group.manageUrl)
            linkText="Manage team"
            linkScreenReaderText="Manage ${group.name} team"
            linkClass="govuk-link govuk-link--no-visited-state"/>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>
</@defaultPage>