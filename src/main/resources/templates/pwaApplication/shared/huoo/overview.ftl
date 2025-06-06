<#-- @ftlvariable name="addHuooUrl" type="String" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="huooOrgs" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooOrganisationUnitRoleView>" -->
<#-- @ftlvariable name="treatyAgreements" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooTreatyAgreementView>" -->
<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="showHolderGuidance" type="Boolean" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Holders, users, operators, and owners (HUOO)" fullWidthColumn=true breadcrumbs=true>

  <#if errorMessage?has_content>
    <@fdsError.singleErrorSummary errorMessage=errorMessage />
  </#if>

  <h1 class="govuk-heading-xl">Holders, users, operators, and owners (HUOO)</h1>

  <#if showHolderGuidance>
    <@fdsInsetText.insetText>
      You can only have a single holder on a PWA.
      <br/><br/>
      To change the holder you must edit the current holder and update the organisation. You can only set the holder to an organisation that you are a member of.
    </@fdsInsetText.insetText>
  </#if>

  <@fdsAction.link linkText="Add holder, user, operator or owner" linkUrl=springUrl(addHuooUrl) linkClass="govuk-link govuk-link--button govuk-button govuk-button--blue" role=true/>

  <#if huooOrgs?has_content>
    <table class="govuk-table">
      <h2 class="govuk-heading-m">Organisation HUOOs</h2>
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
              <@fdsAction.link linkText="Edit" linkUrl=springUrl(org.editUrl) linkClass="govuk-link" linkScreenReaderText="Edit ${org.companyName}"/>
              <#if org.removeUrl?has_content>
                <br/>
                  <@fdsAction.link linkText="Remove" linkClass="govuk-link" linkUrl=springUrl(org.removeUrl) linkScreenReaderText="Remove ${org.companyName}"/>
              </#if>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>

  </#if>

  <#if treatyAgreements?has_content>
    <table class="govuk-table">
      <h2 class="govuk-heading-m">Treaty agreement user</h2>
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
                  <@fdsAction.link linkText="Remove" linkClass="govuk-link" linkUrl=springUrl(agreement.removeUrl) linkScreenReaderText="Remove ${agreement.country}"/>
              </#if>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>
  </#if>
    <@fdsForm.htmlForm>
      <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>