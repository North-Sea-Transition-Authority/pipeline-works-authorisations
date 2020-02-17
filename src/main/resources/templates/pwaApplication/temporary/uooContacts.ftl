<#include '../../layout.ftl'>

<#-- @ftlvariable name="uooList" type="uk.co.ogauthority.pwa.temp.model.contacts.UserOwnerOperatorView[]" -->

<#macro disabledCheckbox checked>
  <div class="govuk-checkboxes">
    <div class="govuk-checkboxes__item">
      <input class="govuk-checkboxes__input" type="checkbox" <#if checked>checked</#if> disabled>
      <label class="govuk-label govuk-checkboxes__label"></label>
    </div>
  </div>
</#macro>

<@defaultPage htmlTitle="UOO contacts" pageHeading="Users, owners, and operator" twoThirdsColumn=false backLink=true>

    <@fdsAction.button buttonText="Add new"/>

    <@fdsWarning.warning>
      You may only have one company fulfilling the "operator" role.
    </@fdsWarning.warning>

  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Company number</th>
      <th class="govuk-table__header" scope="col">Company name</th>
      <th class="govuk-table__header" scope="col">Company address</th>
      <th class="govuk-table__header" scope="col">Roles</th>
      <th class="govuk-table__header" scope="col">Action</th>
    </tr>
    <tbody class="govuk-table__body">
      <#list uooList as uoo>
        <tr class="govuk-table__row">
<#--          ?c treats it as a string rather than a number, preventing formatting-->
          <td class="govuk-table__cell">${uoo.companyHouseNumber?c}</td>
          <td class="govuk-table__cell">${uoo.companyName}</td>
          <td class="govuk-table__cell"><pre class="govuk-body">${uoo.companyAddress}</pre></td>
          <td class="govuk-table__cell">
              <#list uoo.roles as role>
                  ${role}<#if role?index != uoo.roles?size - 1>,</#if>
                <br>
              </#list>
          </td>
          <td class="govuk-table__cell">
            <ul class="govuk-list">
              <li>
                  <@fdsAction.link linkText="Remove" linkUrl=springUrl("/") linkClass="govuk-link"></@fdsAction.link>
              </li>
            </ul>
          </td>
        </tr>
      </#list>
    </tbody>
  </table>

</@defaultPage>