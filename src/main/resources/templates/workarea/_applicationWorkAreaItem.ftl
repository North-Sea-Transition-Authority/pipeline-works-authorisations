<#include '../layout.ftl'>

<#-- @ftlvariable name="item" type="uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem" -->

<#macro referenceColumnHeader>
  <th class="govuk-table__header" scope="col">Application reference</th>
</#macro>

<#macro referenceColumn item noRefText="">
    <#assign viewLinkText=item.getApplicationReference()?has_content?then(item.getApplicationReference(), noRefText) />
    <@fdsAction.link linkText=viewLinkText linkUrl=springUrl(item.getAccessUrl()) linkClass="govuk-link govuk-link--no-visited-state" />
</#macro>

<#macro summaryColumnHeader>
  <th class="govuk-table__header" scope="col">Summary</th>
</#macro>

<#macro summaryColumn item>
  <ul class="govuk-list">
    <li>Project Name: ${item.getProjectName()!""}</li>
    <li>Proposed start date: ${item.getProposedStartDateDisplay()!""}</li>
      <#if item.getOrderedFieldList()?has_content>
        <li>Field: ${item.getOrderedFieldList()?join(", ")}</li>
      </#if>
  </ul>
</#macro>

<#macro statusLabelListItem>
  <li><strong class="govuk-tag govuk-tag--blue"><#nested></strong></li>
</#macro>

<#macro fastTrackLabelListItem item>
  <#if item.wasSubmittedAsFastTrack()>
    <li><strong class="govuk-tag govuk-tag--${item.isFastTrackAccepted()?then("green", "red")}">${item.getFastTrackLabelText()}</strong></li>
  </#if>
</#macro>