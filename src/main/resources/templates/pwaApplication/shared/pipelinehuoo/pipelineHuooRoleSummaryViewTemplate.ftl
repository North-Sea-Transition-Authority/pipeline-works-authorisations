<#include '../../../layout.ftl'>

<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelineHuooUrlFactory" -->
<#-- @ftlvariable name="summaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->


<#macro pipelineHuooRoleSummary summaryView errorKeyPrefix summaryValidationResult urlFactory>
    <#local roleSingular=summaryView.getRoleDisplayText() />
    <h2 class="govuk-heading-m">${summaryView.getRoleDisplayText()}s</h2>

    <#local unassignedPipelineCardId = validationResult.constructObjectId(summaryValidationResult!, (errorKeyPrefix + "-UNASSIGNED-PIPELINES")) />
    <#local unassignedRoleCardId = validationResult.constructObjectId(summaryValidationResult!, (errorKeyPrefix + "-UNASSIGNED-ROLES")) />
    <#local unassignedRoleErrorMessage = validationResult.errorMessageOrEmptyString(summaryValidationResult!, unassignedRoleCardId) />

    <#local invalidSplitErrorId = validationResult.constructObjectId(summaryValidationResult!, (errorKeyPrefix + "-INVALID-SPLITS")) />
    <#local invalidSplitErrorMessage = validationResult.errorMessageOrEmptyString(summaryValidationResult!, invalidSplitErrorId)  />

    <#if invalidSplitErrorId?has_content && invalidSplitErrorMessage?has_content>
        <p id="${invalidSplitErrorId}" class="govuk-error-message">${invalidSplitErrorMessage}</p>
    </#if>

    <#if summaryView.sortedUnassignedPipelineNumbers?has_content>

        <#local useAllPipelinesHeader=!summaryView.pipelinesAndOrgRoleGroupViews?has_content />

        <@pipelineRoleGroup
          pipelineNumberList=useAllPipelinesHeader?then(["All pipelines"], summaryView.sortedUnassignedPipelineNumbers)
          organisationNameList=["No ${roleSingular?lower_case}s assigned"]
          linkText="Assign ${roleSingular?lower_case}s for these pipelines"
          linkUrl=urlFactory.assignUnassignedPipelineOwnersUrl(summaryView.huooRole, summaryView)
          cardId=unassignedPipelineCardId
          summaryValidationResult=summaryValidationResult!
        />
    </#if>

    <!-- use All pipelines when theres on 1 group and no unassigned pipelines -->
    <#local groupViewUseAllPipelinesHeader=summaryView.pipelinesAndOrgRoleGroupViews?size==1 && !summaryView.sortedUnassignedPipelineNumbers?has_content/>
    <#list summaryView.pipelinesAndOrgRoleGroupViews as groupView >
        <@pipelineRoleGroup
          pipelineNumberList=groupView.pipelineNumbers
          organisationNameList=groupView.organisationNames
          linkText="Change ${roleSingular?lower_case}s for these pipelines"
          linkUrl=urlFactory.changeGroupPipelineOwnersUrl(summaryView.huooRole, groupView)
          headerOverrideText=groupViewUseAllPipelinesHeader?then("All pipelines", "")
          cardId="${errorKeyPrefix}-pipeline-group-${groupView?index}"
          summaryValidationResult=summaryValidationResult!/>
    </#list>

    <#if summaryView.sortedUnassignedOrganisationNames?has_content>
        <#local unassignedRoleHeading>
            <@stringUtils.pluraliseWord count=summaryView.sortedUnassignedOrganisationNames?size word=roleSingular />
        </#local>
        <@fdsCard.card cardId=unassignedRoleCardId cardClass=unassignedRoleErrorMessage?has_content?then("fds-card--error", "") >
            <@fdsCard.cardHeader cardHeadingText="${unassignedRoleHeading} not assigned to pipelines" cardHeadingSize="h3" cardHeadingClass="govuk-heading-s govuk-!-padding-bottom-3" cardErrorMessage=unassignedRoleErrorMessage!"" />
            <ol class="govuk-list">
                <#list summaryView.sortedUnassignedOrganisationNames as orgName>
                  <li>${orgName}</li>
                </#list>
            </ol>
        </@fdsCard.card>
    </#if>

    <#if summaryView.getHuooRole()=="HOLDER">
        <#local addRoleUrl = urlFactory.getAddHolderPipelineRoleUrl()/>
        <#local defineSplitUrl = urlFactory.getSplitHolderPipelineUrl()/>
    </#if>
    <#if summaryView.getHuooRole()=="USER">
        <#local addRoleUrl = urlFactory.getAddUserPipelineRoleUrl()/>
        <#local defineSplitUrl = urlFactory.getSplitUserPipelineUrl()/>
    </#if>
    <#if summaryView.getHuooRole()=="OPERATOR">
        <#local addRoleUrl = urlFactory.getAddOperatorPipelineRoleUrl()/>
        <#local defineSplitUrl = urlFactory.getSplitOperatorPipelineUrl()/>
    </#if>
    <#if summaryView.getHuooRole()=="OWNER">
        <#local addRoleUrl = urlFactory.getAddOwnerPipelineRoleUrl()/>
        <#local defineSplitUrl = urlFactory.getSplitOwnerPipelineUrl()/>
    </#if>

    <@fdsAction.link linkText="Select pipelines and assign ${roleSingular?lower_case}s"  linkUrl=springUrl(addRoleUrl) linkClass="govuk-button govuk-button--blue"/>
    <#if (summaryView.totalOrganisationRoleOwners > 1 )>
        <@fdsAction.link linkText="Define pipeline ${roleSingular?lower_case} split"  linkUrl=springUrl(defineSplitUrl) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>


<#macro pipelineRoleGroup pipelineNumberList organisationNameList linkText linkUrl summaryValidationResult headerOverrideText="" cardId="">

    <#local errorMessage = validationResult.errorMessageOrEmptyString(summaryValidationResult!, cardId) />
    <@fdsCard.card cardId=cardId cardClass=errorMessage?has_content?then("fds-card--error", "")>

        <#local joinedPipelineNumbers=pipelineNumberList?join(", ")/>
        <#local header=headerOverrideText?has_content?then(headerOverrideText, joinedPipelineNumbers) />
        <@fdsCard.cardHeader cardHeadingText=header cardHeadingSize="h3" cardHeadingClass="govuk-heading-s govuk-!-padding-bottom-3" cardErrorMessage=errorMessage!""/>
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