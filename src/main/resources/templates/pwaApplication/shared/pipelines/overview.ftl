<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineTaskListItems" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem>" -->
<#-- @ftlvariable name="pipelineUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineUrlFactory" -->
<#-- @ftlvariable name="pipelineSummaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="canImportConsentedPipeline" type="Boolean" -->

<#macro linkButtonBlue text url>
    <@fdsAction.link linkText=text linkUrl=url linkClass="govuk-button govuk-button--blue" />
</#macro>

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" fullWidthColumn=true breadcrumbs=true>

    <@validationResult.singleErrorSummary summaryValidationResult=pipelineSummaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=pipelineSummaryValidationResult! />

    <#if !pipelineTaskListItems?has_content>
        <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
    <#if canImportConsentedPipeline>
        <@linkButtonBlue text="Modify consented pipeline" url=springUrl(pipelineUrlFactory.getModifyPipelineUrl()) />
    </#if>

    <#list pipelineTaskListItems as pipeline>

        <#assign cardId = validationResult.constructObjectId(pipelineSummaryValidationResult!, pipeline.padPipelineId) />

        <#assign hasErrors = validationResult.hasErrors(pipelineSummaryValidationResult!, cardId) />
        <#assign cardErrorMessage = validationResult.errorMessageOrEmptyString(pipelineSummaryValidationResult!, cardId) />

        <@fdsCard.card cardId=cardId cardClass=hasErrors?then("fds-card--error", "")>

          <@fdsCard.cardHeader cardHeadingText="${pipeline.getPipelineName()}" cardErrorMessage=cardErrorMessage />

          <hr class="govuk-section-break govuk-section-break--m"/>

            <@fdsTaskList.taskList>
                <#list pipeline.getTaskList() as task>
                    <#if task.taskInfoList?has_content>
                        <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route/>
                    <#else>
                        <@fdsTaskList.taskListItem itemText=task.taskName itemUrl=springUrl(task.route) completed=task.completed/>
                    </#if>
                </#list>
            </@fdsTaskList.taskList>
        </@fdsCard.card>

    </#list>

    <#if pipelineTaskListItems?size gt 4>
        <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
        <#if canImportConsentedPipeline>
            <@linkButtonBlue text="Modify consented pipeline" url=springUrl(pipelineUrlFactory.getModifyPipelineUrl()) />
        </#if>
    </#if>

    <#if pipelineTaskListItems?has_content>
      <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>