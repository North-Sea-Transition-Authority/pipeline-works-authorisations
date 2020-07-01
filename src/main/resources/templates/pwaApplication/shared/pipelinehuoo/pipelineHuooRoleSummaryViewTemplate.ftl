<#include '../../../layout.ftl'>

<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->


<#macro pipelineHuooRoleSummary summaryView >
    <#local roleSingular=summaryView.getRoleDisplayText() />

    <#if summaryView.sortedUnassignedPipelineNumbers?hasContent>
        <#local useAllPipelinesHeader=!summaryView.pipelinesAndOrgRoleGroupViews?hasContent />

        <@pipelineRoleGroup
          pipelineNumberList=useAllPipelinesHeader?then(["All pipelines"], summaryView.sortedUnassignedPipelineNumbers)
          organisationNameList=["No ${roleSingular?lowerCase}s assigned"]
          linkText="Assign ${roleSingular?lowerCase}s for these pipelines"/>
    </#if>

    <!-- use All pipelines when theres on 1 group and no unassigned pipelines -->
    <#local groupViewUseAllPipelinesHeader=summaryView.pipelinesAndOrgRoleGroupViews?size==1 && !summaryView.sortedUnassignedPipelineNumbers?hasContent/>
    <#list summaryView.pipelinesAndOrgRoleGroupViews as groupView >
        <@pipelineRoleGroup
          pipelineNumberList=groupView.pipelineNumbers
          organisationNameList=groupView.organisationNames
          linkText="Change ${roleSingular?lowerCase}s for these pipelines"
          headerOverrideText=groupViewUseAllPipelinesHeader?then("All pipelines", "")/>
    </#list>

    <#if summaryView.sortedUnassignedOrganisationNames?hasContent>
        <#local unassignedRoleHeading=(summaryView.sortedUnassignedOrganisationNames?size>1)?then("${roleSingular}s", roleSingular) />
        <@fdsCard.card>
            <@fdsCard.cardHeader cardHeadingText="${unassignedRoleHeading} not assigned to pipelines" cardHeadingSize="h3" cardHeadingClass="govuk-heading-s" />
          <ol class="govuk-list">
              <#list summaryView.sortedUnassignedOrganisationNames as orgName>
                <li>${orgName}</li>
              </#list>
          </ol>
        </@fdsCard.card>
    </#if>

</#macro>


<#macro pipelineRoleGroup pipelineNumberList organisationNameList linkText headerOverrideText="">
    <@fdsCard.card>
        <#local joinedPipelineNumbers=pipelineNumberList?join(", ")/>
        <#local header=headerOverrideText?hasContent?then(headerOverrideText, joinedPipelineNumbers) />
        <@fdsCard.cardHeader cardHeadingText=header cardHeadingSize="h3" cardHeadingClass="govuk-heading-s" />
      <ol class="govuk-list">
          <#list organisationNameList as orgName>
            <li>${orgName}</li>
          </#list>
      </ol>
        <@fdsAction.link linkText=linkText linkClass="govuk-link" linkUrl=springUrl("/#") linkScreenReaderText=linkText + ": " + joinedPipelineNumbers />
    </@fdsCard.card>
</#macro>