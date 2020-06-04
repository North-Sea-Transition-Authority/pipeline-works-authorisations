<#include '../../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="String" -->
<#-- @ftlvariable name="isOptionsVariation" type="java.lang.Boolean" -->
<#-- @ftlvariable name="isFastTrack" type="java.lang.Boolean" -->
<#-- @ftlvariable name="caseOfficerCandidates" type="java.util.Map<String, String>" -->

<@defaultPage htmlTitle="Accept application ${appRef}" pageHeading="Accept application ${appRef}" breadcrumbs=true>

    <#if isOptionsVariation || isFastTrack>

        <@fdsWarning.warning>

            <#if isFastTrack>
              This application is being fast-tracked, consider the proposed start date before accepting.
            </#if>

            <#if isOptionsVariation>
              This application is an Options variation, consider all options provided before accepting.
            </#if>

        </@fdsWarning.warning>

    </#if>

    <@fdsForm.htmlForm>

      <@fdsSearchSelector.searchSelectorEnhanced path="form.caseOfficerPersonId" options=caseOfficerCandidates labelText="Case officer" />

      <@fdsAction.submitButtons primaryButtonText="Accept application" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workAreaUrl) />

    </@fdsForm.htmlForm>

</@defaultPage>