<#include '../../layout.ftl'>

<#-- @ftlvariable name="responders" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="consulteeGroupName" type="String" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Assign responder" topNavigation=true fullWidthColumn=true breadcrumbs=true>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
      </#if>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <@grid.gridRow>
    <@grid.fullColumn>
      <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />
    </@grid.fullColumn>
  </@grid.gridRow>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>

      <span class="govuk-caption-m">${consulteeGroupName}</span>
      <h2 class="govuk-heading-l">Assign responder</h2>

      <@fdsForm.htmlForm>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.responderPersonId" options=responders labelText="Select a responder for the consultation request"/>

        <@fdsAction.submitButtons primaryButtonText="Assign" linkSecondaryAction=true secondaryLinkText="Back to tasks" linkSecondaryActionUrl=springUrl(cancelUrl)/>

      </@fdsForm.htmlForm>

    </@grid.twoThirdsColumn>
  </@grid.gridRow>

</@defaultPage>