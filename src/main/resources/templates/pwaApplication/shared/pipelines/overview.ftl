<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineTaskListItems" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem>" -->
<#-- @ftlvariable name="pipelineUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineUrlFactory" -->
<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="canShowAddBundleButton" type="Boolean" -->

<#macro linkButtonBlue text url>
    <@fdsAction.link linkText=text linkUrl=url linkClass="govuk-button govuk-button--blue" />
</#macro>

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" fullWidthColumn=true breadcrumbs=true>

    <#if !pipelineTaskListItems?has_content>
        <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
    <#if canShowAddBundleButton>
        <@linkButtonBlue text="Add bundle" url=springUrl(pipelineUrlFactory.getAddBundleUrl()) />
    </#if>

    <#list pipelineTaskListItems as pipeline>

        <@fdsCard.card>

            <span class="govuk-caption-l">${pipeline.length}m ${pipeline.pipelineType.displayName}</span>
            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineNumber}" />

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
    </#if>

    <#if pipelineTaskListItems?has_content>
        <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) errorMessage=errorMessage!/>
    </@fdsForm.htmlForm>

</@defaultPage>