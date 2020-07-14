<#include '../../../layout.ftl'>

<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelineHuooUrlFactory" -->


<#macro pipelineHuooRoleSummary summaryView urlFactory>
    <#local roleSingular=summaryView.getRoleDisplayText() />

    <#if summaryView.sortedUnassignedPipelineNumbers?hasContent>
        <#local useAllPipelinesHeader=!summaryView.pipelinesAndOrgRoleGroupViews?hasContent />

        <@pipelineRoleGroup
          pipelineNumberList=useAllPipelinesHeader?then(["All pipelines"], summaryView.sortedUnassignedPipelineNumbers)
          organisationNameList=["No ${roleSingular?lowerCase}s assigned"]
          linkText="Assign ${roleSingular?lowerCase}s for these pipelines"
          linkUrl=urlFactory.assignUnassignedPipelineOwnersUrl(summaryView.huooRole, summaryView)/>
    </#if>

    <!-- use All pipelines when theres on 1 group and no unassigned pipelines -->
    <#local groupViewUseAllPipelinesHeader=summaryView.pipelinesAndOrgRoleGroupViews?size==1 && !summaryView.sortedUnassignedPipelineNumbers?hasContent/>
    <#list summaryView.pipelinesAndOrgRoleGroupViews as groupView >
        <@pipelineRoleGroup
          pipelineNumberList=groupView.pipelineNumbers
          organisationNameList=groupView.organisationNames
          linkText="Change ${roleSingular?lowerCase}s for these pipelines"
          linkUrl=urlFactory.changeGroupPipelineOwnersUrl(summaryView.huooRole, groupView)
          headerOverrideText=groupViewUseAllPipelinesHeader?then("All pipelines", "")/>
    </#list>

    <#if summaryView.sortedUnassignedOrganisationNames?hasContent>
        <#local unassignedRoleHeading>
            <@stringUtils.pluraliseWord count=summaryView.sortedUnassignedOrganisationNames?size word=roleSingular />
        </#local>
        <@fdsCard.card>
            <@fdsCard.cardHeader cardHeadingText="${unassignedRoleHeading} not assigned to pipelines" cardHeadingSize="h3" cardHeadingClass="govuk-heading-s govuk-!-padding-bottom-3" />
            <ol class="govuk-list">
                <#list summaryView.sortedUnassignedOrganisationNames as orgName>
                  <li>${orgName}</li>
                </#list>
            </ol>
        </@fdsCard.card>
    </#if>

</#macro>


<#macro pipelineRoleGroup pipelineNumberList organisationNameList linkText linkUrl headerOverrideText="">
    <@fdsCard.card>
        <#local joinedPipelineNumbers=pipelineNumberList?join(", ")/>
        <#local header=headerOverrideText?hasContent?then(headerOverrideText, joinedPipelineNumbers) />
        <@fdsCard.cardHeader cardHeadingText=header cardHeadingSize="h3" cardHeadingClass="govuk-heading-s govuk-!-padding-bottom-3"/>
        <ol class="govuk-list">
            <#list organisationNameList as orgName>
              <li>${orgName}</li>
            </#list>
        </ol>
        <@fdsForm.htmlForm actionUrl=springUrl(linkUrl) >
            <@fdsAction.button buttonText=linkText buttonClass="fds-link-button" buttonScreenReaderText=linkText + ": " + joinedPipelineNumbers />
        </@fdsForm.htmlForm>

    </@fdsCard.card>
</#macro>