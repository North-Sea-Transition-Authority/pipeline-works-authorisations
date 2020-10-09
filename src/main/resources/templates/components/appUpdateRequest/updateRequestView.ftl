<#include '../../layout.ftl'>

<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->

<#macro banner view canUpdate=false taskListUrl="">
  <@fdsContactPanel.contactPanel headingText="Further information requested" contentHeadingText=view.requestReason contentHeadingCaption="Requested on ${view.getRequestedTimestampDisplay()}">
      <#if canUpdate>
          <@fdsAction.link linkText="Update application" linkClass="govuk-button govuk-button--negative" linkUrl=springUrl(taskListUrl) role=true/>
      </#if>
  </@fdsContactPanel.contactPanel>
</#macro>