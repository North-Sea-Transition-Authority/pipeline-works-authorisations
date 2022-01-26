<#import '../../../../diffedViews/diffedHuooView.ftl' as diffedHuooView>
<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="diffedHuooSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups" -->
<#-- @ftlvariable name="consentVersionSearchSelectorItems" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="viewPwaPipelineUrl" type="String" -->

<#macro tab diffedHuooSummary>

    <@fdsForm.htmlForm actionUrl=springUrl(viewPwaPipelineUrl)>
        <@fdsSearchSelector.searchSelectorEnhanced path="form.huooVersionId" options=consentVersionSearchSelectorItems labelText="Select consent version" inputClass="govuk-!-width-one-half"/>
        <@fdsAction.button buttonText="Show version" buttonScreenReaderText="Show HUOO information for the selected consent version"/>
    </@fdsForm.htmlForm>

    <@diffChanges.toggler/>
    
    <#if diffedHuooSummary?has_content>
        <#assign pipelinesDataRowKey = "Pipeline sections"/>
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.holderOrgRolePipelineGroups  role="Holders" showNoRolesWarning=false pipelinesDataRowKey=pipelinesDataRowKey />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.userOrgRolePipelineGroups  role="Users" showNoRolesWarning=false pipelinesDataRowKey=pipelinesDataRowKey />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.operatorOrgRolePipelineGroups  role="Operators" showNoRolesWarning=false pipelinesDataRowKey=pipelinesDataRowKey />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.ownerOrgRolePipelineGroups  role="Owners" showNoRolesWarning=false pipelinesDataRowKey=pipelinesDataRowKey />
    </#if>


</#macro>