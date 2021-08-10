<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="allRolePipelineGroupView" type="java.util.List<uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.AllRoleDiffablePipelineGroupView>"-->
<#-- @ftlvariable name="orgRoleNameToTextMap" type="java.util.Map<String, String>"-->

<div id="huooSection" style="page-break-before: always;">

  <h2 class="govuk-heading-l">${sectionName}</h2>

  <div class="huooRolePart">
    <span>PART I </span> </br>
    <span class="roleTypeTxt">The ${orgRoleNameToTextMap["HOLDER"]} </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.holderOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART II </span> </br>
    <span class="roleTypeTxt">The ${orgRoleNameToTextMap["USER"]} </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.userOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART III </span> </br>
    <span class="roleTypeTxt">The ${orgRoleNameToTextMap["OPERATOR"]} </span>
    <@orgRoleAndPipelines orgRolePipelineGroups=allRolePipelineGroupView.operatorOrgRolePipelineGroups/>
  </div>

  <div class="huooRolePart">
    <span>PART IV </span> </br>
    <span class="roleTypeTxt">The ${orgRoleNameToTextMap["OWNER"]} </span>
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

      <#if orgRolePipelineGroup.orgRoleIsTreatyAgreement>
          <p class="huooOrgDescription huooTreatyDescription">
            ${orgRolePipelineGroup.treatyAgreementText}
          </p>
      <#else>
        <p class="huooOrgDescription">
          ${orgRolePipelineGroup.roleOwnerName.value}, whose registered office is ${orgRolePipelineGroup.companyAddress}. Registered Company Number ${orgRolePipelineGroup.companyNumber}
        </p>
      </#if>
    </div>
  </#list>

</#macro>