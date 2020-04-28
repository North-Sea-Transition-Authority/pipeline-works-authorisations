<#-- @ftlvariable name="addHuooUrl" type="String" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="huooOrgs" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooOrganisationUnitRoleView>" -->
<#-- @ftlvariable name="treatyAgreements" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooTreatyAgreementView>" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Holders, users, operators, and owners" pageHeading="Holders, users, operators, and owners" fullWidthColumn=true breadcrumbs=true>

  <@fdsAction.link linkText="Add holder, user, operator or owner" linkUrl=springUrl(addHuooUrl) linkClass="govuk-link govuk-link--button govuk-button govuk-button--blue" role=true/>

  <#if huooOrgs?has_content>
    <table class="govuk-table">
      <thead class="govuk-table__header">
      <tr class="govuk-table__row">
        <th class="govuk-table__cell" scope="col">Company number</th>
        <th class="govuk-table__cell" scope="col">Legal entity name</th>
        <th class="govuk-table__cell" scope="col">Legal entity address</th>
        <th class="govuk-table__cell" scope="col">Roles</th>
        <th class="govuk-table__cell" scope="col">Actions</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <#list huooOrgs as org>
        <tr class="govuk-table__row">
          <td class="govuk-table__cell">${org.registeredNumber!}</td>
          <td class="govuk-table__cell">${org.companyName}</td>
          <td class="govuk-table__cell">${org.companyAddress!}</td>
          <td class="govuk-table__cell">${org.roles}</td>
          <td class="govuk-table__cell">
              <@fdsAction.link linkText="Edit" linkUrl=springUrl(org.editUrl) linkClass="govuk-link"/>
              <#if org.removeUrl?has_content>
                <br/>
                  <@fdsForm.htmlForm actionUrl=springUrl(org.removeUrl)>
                      <@fdsAction.button buttonText="Remove" buttonClass="fds-link-button"/>
                  </@fdsForm.htmlForm>
              </#if>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>

  </#if>

  <#if treatyAgreements?has_content>
    <table class="govuk-table">
      <thead class="govuk-table__header">
      <tr class="govuk-table__row">
        <th class="govuk-table__cell" scope="col">Country</th>
        <th class="govuk-table__cell" scope="col">Treaty agreement text</th>
        <th class="govuk-table__cell" scope="col">Roles</th>
        <th class="govuk-table__cell" scope="col">Actions</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <#list treatyAgreements as agreement>
        <tr class="govuk-table__row">
          <td class="govuk-table__cell">${agreement.country}</td>
          <td class="govuk-table__cell">${agreement.treatyAgreementText}</td>
          <td class="govuk-table__cell">${agreement.roles}</td>
          <td class="govuk-table__cell">
              <#if agreement.removeUrl?has_content>
                  <@fdsForm.htmlForm actionUrl=springUrl(agreement.removeUrl)>
                      <@fdsAction.button buttonText="Remove" buttonClass="fds-link-button"/>
                  </@fdsForm.htmlForm>
              </#if>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>
  </#if>
    <@fdsForm.htmlForm>
      <@fdsAction.submitButtons errorMessage=errorMessage!"" primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>