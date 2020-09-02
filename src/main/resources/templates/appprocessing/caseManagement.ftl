<#-- @ftlvariable name="consultationUrl" type="java.lang.String" -->
<#-- @ftlvariable name="assignCaseOfficerUrl" type="java.lang.String" -->
<#-- @ftlvariable name="requestUpdateUrl" type="java.lang.String" -->
<#-- @ftlvariable name="hasAssignCaseOfficerPermission" type="java.lang.Boolean" -->
<#-- @ftlvariable name="canRequestApplicationUpdate" type="java.lang.Boolean" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} case management" topNavigation=true fullWidthColumn=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <@fdsAction.link linkText="Consultations" linkClass="govuk-link govuk-link--no-visited-state category-list__link" linkUrl=springUrl(consultationUrl)/>
  <span class="govuk-hint">Send application to consultee groups for comment</span>

  <#if hasAssignCaseOfficerPermission>
    <@fdsAction.link linkText="Reassign case officer" linkClass="govuk-link govuk-link--no-visited-state category-list__link" linkUrl=springUrl(assignCaseOfficerUrl)/>
    <span class="govuk-hint">Reassign an application that already has a case officer assigned</span>
  </#if>

  <#if canRequestApplicationUpdate>
    <@fdsAction.link linkText="Request application update" linkClass="govuk-link govuk-link--no-visited-state category-list__link" linkUrl=springUrl(requestUpdateUrl)/>
    <span class="govuk-hint">Request an update to the application from the submitter</span>
  </#if>

</@defaultPage>