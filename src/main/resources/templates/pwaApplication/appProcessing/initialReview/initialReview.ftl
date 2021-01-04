<#include '../../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="String" -->
<#-- @ftlvariable name="isOptionsVariation" type="java.lang.Boolean" -->
<#-- @ftlvariable name="isFastTrack" type="java.lang.Boolean" -->
<#-- @ftlvariable name="caseOfficerCandidates" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Accept application ${appRef}" breadcrumbs=true fullWidthColumn=true>

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

          <#if isFastTrack>
              <@fdsWarning.warning>
                This application is being fast-tracked, consider the proposed start of works date before accepting.
              </@fdsWarning.warning>
          </#if>

          <#if isOptionsVariation>
              <@fdsWarning.warning>
                This application is an Options variation, consider all options provided before accepting.
              </@fdsWarning.warning>
          </#if>

          <@fdsForm.htmlForm>

              <@fdsSearchSelector.searchSelectorEnhanced path="form.caseOfficerPersonId" options=caseOfficerCandidates labelText="Case officer" />

              <@fdsAction.submitButtons primaryButtonText="Accept application" linkSecondaryAction=true secondaryLinkText="Back to case management" linkSecondaryActionUrl=springUrl(cancelUrl) />

          </@fdsForm.htmlForm>

      </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>