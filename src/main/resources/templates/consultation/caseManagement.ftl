<#-- @ftlvariable name="consultationUrl" type="java.lang.String" -->
<#-- @ftlvariable name="assignCaseOfficerUrl" type="java.lang.String" -->
<#-- @ftlvariable name="hasAssignCaseOfficerPermission" type="java.lang.Boolean" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Case management" pageHeading="Case management" topNavigation=true fullWidthColumn=true>


  <@fdsAction.link linkText="Consultations" linkClass="govuk-link govuk-link--no-visited-state category-list__link" linkUrl=springUrl(consultationUrl)/>
  <span class="govuk-hint">Send application to consultee groups for comment</span>

  <#if hasAssignCaseOfficerPermission>
    <@fdsAction.link linkText="Reassign case officer" linkClass="govuk-link govuk-link--no-visited-state category-list__link" linkUrl=springUrl(assignCaseOfficerUrl)/>
    <span class="govuk-hint">Reassign an application that already has a case officer assigned</span>
  </#if>


</@defaultPage>