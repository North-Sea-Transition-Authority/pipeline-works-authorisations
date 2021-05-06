<#import '../../../../diffedViews/diffedHuooView.ftl' as diffedHuooView>
<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="diffedHuooSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups" -->
<#-- @ftlvariable name="consentVersionSearchSelectorItems" type="java.util.Map<java.lang.String, java.lang.String>" -->


<#macro tab diffedHuooSummary>

    <@fdsForm.htmlForm actionUrl=springUrl(viewPwaPipelineUrl)>
        <@fdsSearchSelector.searchSelectorEnhanced path="form.consentId" options=consentVersionSearchSelectorItems labelText="Select version" />
        <@fdsAction.button buttonText="Show version"/>
    </@fdsForm.htmlForm>

    
    <#if diffedHuooSummary?has_content>
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.holderOrgRolePipelineGroups  role="Holders" showNoRolesWarning=false/>
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.userOrgRolePipelineGroups  role="Users" showNoRolesWarning=false />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.operatorOrgRolePipelineGroups  role="Operators" showNoRolesWarning=false />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.ownerOrgRolePipelineGroups  role="Owners" showNoRolesWarning=false />
    </#if>


</#macro>