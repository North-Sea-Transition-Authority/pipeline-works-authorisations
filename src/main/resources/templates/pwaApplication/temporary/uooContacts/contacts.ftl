<#include '../../../layout.ftl'>

<#-- @ftlvariable name="uooCompanyList" type="java.util.List<uk.co.ogauthority.pwa.temp.model.contacts.UooCompanyView>" -->
<#-- @ftlvariable name="uooTreatyList" type="java.util.List<uk.co.ogauthority.pwa.temp.model.contacts.UooAgreementView>" -->
<#-- @ftlvariable name="newUooUrl" type="java.lang.String" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Users, operator, and owners" pageHeading="Users, operator, and owners" twoThirdsColumn=false backLink=true backLinkUrl=springUrl(taskListUrl)>

    <@fdsAction.link linkText="Add new user, operator, or owner" linkUrl=springUrl(newUooUrl) linkClass="govuk-button"/>

    <@fdsInsetText.insetText>
      You may only have one company fulfilling the "operator" role.
    </@fdsInsetText.insetText>

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
    <#list uooCompanyList as uoo>
      <tr class="govuk-table__row">
          <#-- ?c treats it as a string rather than a number, preventing formatting -->
        <td class="govuk-table__cell">${uoo.companiesHouseNumber?c}</td>
        <td class="govuk-table__cell">${uoo.companyName}</td>
        <td class="govuk-table__cell">
          <pre class="govuk-body">${uoo.companyAddress}</pre>
        </td>
        <td class="govuk-table__cell">
            <#list uoo.roles as role>
                ${role}<#if role?index != uoo.roles?size - 1>,</#if>
              <br>
            </#list>
        </td>
        <td class="govuk-table__cell">
          <ul class="govuk-list">
            <li>
                <@fdsAction.link linkText="Edit" linkUrl=springUrl("/") linkClass="govuk-link"></@fdsAction.link>
            </li>
            <li>
                <@fdsAction.link linkText="Remove" linkUrl=springUrl("/") linkClass="govuk-link"></@fdsAction.link>
            </li>
          </ul>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Treaty</th>
      <th class="govuk-table__header" scope="col">Roles</th>
      <th class="govuk-table__header" scope="col">Action</th>
    </tr>
    <tbody class="govuk-table__body">
    <#list uooTreatyList as uoo>
      <tr class="govuk-table__row">
          <#-- ?c treats it as a string rather than a number, preventing formatting -->
        <td class="govuk-table__cell">${uoo.agreementText}</td>
        <td class="govuk-table__cell">
            <#list uoo.roles as role>
                ${role}<#if role?index != uoo.roles?size - 1>,</#if>
              <br>
            </#list>
        </td>
        <td class="govuk-table__cell">
          <ul class="govuk-list">
            <li>
                <@fdsAction.link linkText="Edit" linkUrl=springUrl("/") linkClass="govuk-link"></@fdsAction.link>
            </li>
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