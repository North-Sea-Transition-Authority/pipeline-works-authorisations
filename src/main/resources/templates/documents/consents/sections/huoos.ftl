<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="allRolePipelineGroupView" type="java.util.List<uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.AllRoleDiffablePipelineGroupView>"-->

<div id="huooSection" style="page-break-before: always;">

  <h2 class="govuk-heading-l">${sectionName}</h2>

  <div class="huooRolePart">
    <span>PART I </span> </br>
    <span class="roleTypeTxt">The Holders </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.holderOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART II </span> </br>
    <span class="roleTypeTxt">The Users </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.userOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART III </span> </br>
    <span class="roleTypeTxt">The Operators </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.operatorOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART IV </span> </br>
    <span class="roleTypeTxt">The Owners </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.ownerOrgRolePipelineGroups/>
  </div>

</div>

<#macro orgRoleAndPipelines orgRolePipelineGroups>

  <#list orgRolePipelineGroups as orgRolePipelineGroup>
    <div class="orgRoleAndPipelines">

      <#if orgRolePipelineGroup.showEachPipeline>
        <p class="huooPipelines">
          <#list orgRolePipelineGroup.pipelineAndSplitsList as pipelineAndSplits>
            ${pipelineAndSplits}<#sep>,
          </#list>
        </p>
      </#if>

      <p class="huooOrgDescription">
        ${orgRolePipelineGroup.roleOwnerName.value}, whose registered office is ${orgRolePipelineGroup.companyAddress}. Registered Company Number ${orgRolePipelineGroup.companyNumber}
      </p>
    </div>
  </#list>

</#macro>