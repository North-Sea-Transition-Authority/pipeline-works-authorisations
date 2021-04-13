<#include '../../pwaLayoutImports.ftl'>
<#import '../../diffedViews/diffedHuooView.ftl' as diffedHuooView>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="diffedAllOrgRolePipelineGroups" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="huooDetails">${sectionDisplayText}</h2>

    <@diffedHuooView.huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.holderOrgRolePipelineGroups  role="Holders" />
    <@diffedHuooView.huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.userOrgRolePipelineGroups  role="Users" />
    <@diffedHuooView.huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.operatorOrgRolePipelineGroups  role="Operators" />
    <@diffedHuooView.huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.ownerOrgRolePipelineGroups  role="Owners" />

</div>







