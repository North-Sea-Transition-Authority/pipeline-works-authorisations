<#include '../../layout.ftl'>

<#-- @ftlvariable name="contacts" type="java.util.List<uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView>" -->
<#-- @ftlvariable name="linkToTaskList" type="java.lang.String" -->

<@defaultPage htmlTitle="PWA contacts" pageHeading="PWA contacts" twoThirdsColumn=false backLink=true backLinkUrl=springUrl(linkToTaskList)>
    <@fdsInsetText.insetText>
      PWA contacts are people who are authorised to make changes to any applications related to this PWA.
    </@fdsInsetText.insetText>
  <@fdsAction.button buttonText="Add contact"/>
  <table class="govuk-table">
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header" scope="col">Name</th>
        <th class="govuk-table__header" scope="col">Contact details</th>
        <th class="govuk-table__header" scope="col">Roles</th>
        <th class="govuk-table__header" scope="col">Actions</th>
      </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list contacts as contact>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${contact.fullName}</td>
        <td class="govuk-table__cell">
          <ul class="govuk-list">
            <li>${contact.emailAddress}</li>
            <li>${contact.telephoneNo}</li>
          </ul>
        </td>
        <td class="govuk-table__cell">
            <#list contact.roleViews?sort_by("displaySequence") as roleView>
                ${roleView.title}
              <br>
            </#list>
        </td>
        <td class="govuk-table__cell">
          <ul class="govuk-list">
            <li>
                <@fdsAction.link linkText="Edit" linkUrl=springUrl(contact.editRoute) linkClass="govuk-link"/>
            </li>
            <li>
                <@fdsAction.link linkText="Remove" linkUrl=springUrl(contact.removeRoute) linkClass="govuk-link"/>
            </li>
          </ul>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

</@defaultPage>