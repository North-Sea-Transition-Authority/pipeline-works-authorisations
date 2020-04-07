<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Update users" pageHeading="Update users" breadcrumbs=true>

  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput path="form.holders" labelText="Users" />
    <@fdsAction.button buttonText="Save and continue" />
  </@fdsForm.htmlForm>

  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">Ident number</th>
      <th class="govuk-table__header" scope="col">Current users</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list 1..6 as i>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">PL1</td>
        <td class="govuk-table__cell">${i}</td>
        <td class="govuk-table__cell">
          Conocophillips
          <br/>
          Taqa Brittani
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

</@defaultPage>