<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineTaskListItems" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListItem>" -->
<#-- @ftlvariable name="pipelineUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PipelineUrlFactory" -->
<#-- @ftlvariable name="pipelineSummaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="canImportConsentedPipeline" type="Boolean" -->

<#macro linkButtonBlue text url>
    <@fdsAction.link linkText=text linkUrl=url linkClass="govuk-button govuk-button--blue" />
</#macro>

<@defaultPage htmlTitle="Pipelines" fullWidthColumn=true breadcrumbs=true>

    <@validationResult.singleErrorSummary summaryValidationResult=pipelineSummaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=pipelineSummaryValidationResult! />

    <h1 class="govuk-heading-xl">Pipelines</h1>

    <#if !pipelineTaskListItems?has_content>
        <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    <@fdsAction.buttonGroup>
      <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
      <#if canImportConsentedPipeline>
        <@linkButtonBlue text="Modify consented pipeline" url=springUrl(pipelineUrlFactory.getModifyPipelineUrl()) />
      </#if>
      <@linkButtonBlue text="Transfer pipeline from another PWA" url=springUrl(pipelineUrlFactory.getClaimPipelineUrl()) />
    </@fdsAction.buttonGroup>

    <#list pipelineTaskListItems as pipeline>

        <#assign cardId = validationResult.constructObjectId(pipelineSummaryValidationResult!, pipeline.padPipelineId) />

        <#assign hasErrors = validationResult.hasErrors(pipelineSummaryValidationResult!, cardId) />
        <#assign cardErrorMessage = validationResult.errorMessageOrEmptyString(pipelineSummaryValidationResult!, cardId) />

        <@fdsCard.card cardId=cardId cardClass=hasErrors?then("fds-card--error", "")>

            <@fdsCard.cardHeader cardHeadingText="${pipeline.getPipelineName()}" cardErrorMessage=cardErrorMessage>
                <@fdsCard.cardAction cardLinkText="Remove"
                cardLinkScreenReaderText="${pipeline.getPipelineName()}"
                cardLinkUrl=springUrl(pipelineUrlFactory.getRemovePipelineUrl(pipeline.padPipelineId))
                />
            </@fdsCard.cardHeader>

            <#if pipeline.withdrawnTransfer>
                <p class="govuk-tag govuk-tag--red">TRANSFER WITHDRAWN</p>
            <#else>
                <#if pipeline.pipelineStatus != "IN_SERVICE" || pipeline.withdrawnTransfer>
                    <p class="govuk-tag">${pipeline.pipelineStatus.displayText}</p>
                </#if>
                <#if pipeline.hasTasks>
                    <#if pipeline.pipelineStatus == "IN_SERVICE">
                        <hr class="govuk-section-break govuk-section-break--m"/>
                    </#if>
                    <@fdsTaskList.taskList>
                        <#list pipeline.getTaskList() as task>
                            <@pwaTaskListItem.taskInfoItem
                                taskName=task.taskName
                                taskInfoList=task.taskInfoList
                                route=task.route
                                isCompleted=task.completed
                                linkScreenReaderText="for ${pipeline.getPipelineName()}"/>
                        </#list>
                    </@fdsTaskList.taskList>
                </#if>
            </#if>
        </@fdsCard.card>

    </#list>

    <#if pipelineTaskListItems?size gt 4>
      <@fdsAction.buttonGroup>
        <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
        <#if canImportConsentedPipeline>
            <@linkButtonBlue text="Modify consented pipeline" url=springUrl(pipelineUrlFactory.getModifyPipelineUrl()) />
        </#if>
      </@fdsAction.buttonGroup>
    </#if>

    <#if pipelineTaskListItems?has_content>
      <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>
