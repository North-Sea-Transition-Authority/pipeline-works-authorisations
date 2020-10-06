<#include '../../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="String" -->
<#-- @ftlvariable name="isOptionsVariation" type="java.lang.Boolean" -->
<#-- @ftlvariable name="isFastTrack" type="java.lang.Boolean" -->
<#-- @ftlvariable name="caseOfficerCandidates" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<@defaultPage htmlTitle="Accept application ${appRef}" breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

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

      <@fdsAction.submitButtons primaryButtonText="Accept application" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workAreaUrl) />

    </@fdsForm.htmlForm>

</@defaultPage>