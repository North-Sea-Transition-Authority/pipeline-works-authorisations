<#import '../../../../diffedViews/diffedHuooView.ftl' as diffedHuooView>
<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="diffedHuooSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups" -->


<#macro tab diffedHuooSummary>

    
    <#if diffedHuooSummary?has_content>
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.holderOrgRolePipelineGroups  role="Holders" showNoRolesWarning=false/>
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.userOrgRolePipelineGroups  role="Users" showNoRolesWarning=false />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.operatorOrgRolePipelineGroups  role="Operators" showNoRolesWarning=false />
        <@diffedHuooView.huooDetails diffedHuoos=diffedHuooSummary.ownerOrgRolePipelineGroups  role="Owners" showNoRolesWarning=false />
    </#if>


</#macro>