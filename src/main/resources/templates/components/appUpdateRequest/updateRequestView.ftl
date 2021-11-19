<#include '../../layout.ftl'>

<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->

<#macro banner view canUpdate=false taskListUrl="">
  <#assign headingCaptions = ["Requested on ${view.getRequestedTimestampDisplay()}", "Update due on ${view.getDeadlineTimestampDisplay()}"]/>
    <#assign formattedRequestText>
        <span class="further-info-text-block">
          <@multiLineText.multiLineText blockClass="further-info-text-block">${view.requestReason!}</@multiLineText.multiLineText>
        </span>
    </#assign>
  <@fdsContactPanel.contactPanel headingText="Further information requested" contentHeadingText=formattedRequestText contentHeadingCaptionList=headingCaptions>
      <#if canUpdate>
          <@fdsAction.link linkText="Update application" linkClass="govuk-button govuk-button--negative" linkUrl=springUrl(taskListUrl) role=true/>
      </#if>
  </@fdsContactPanel.contactPanel>
</#macro>