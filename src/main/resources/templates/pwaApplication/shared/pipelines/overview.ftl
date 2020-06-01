<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineTaskListItems" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem>" -->
<#-- @ftlvariable name="addPipelineUrl" type="String" -->
<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->

<#assign addPipeButton>
    <@fdsAction.link linkText="Add pipeline" linkUrl=springUrl(addPipelineUrl) linkClass="govuk-button govuk-button--blue" />
</#assign>

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" fullWidthColumn=true breadcrumbs=true>

    <#if !pipelineTaskListItems?has_content>
        <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    ${addPipeButton}

    <#list pipelineTaskListItems as pipeline>

        <@fdsCard.card>

            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineNumber} ${pipeline.pipelineType.displayName}" />

            <@pipelineOverview.header pipeline=pipeline />

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
      ${addPipeButton}
    </#if>

    <#if pipelineTaskListItems?has_content>
        <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) errorMessage=errorMessage!/>
    </@fdsForm.htmlForm>

</@defaultPage>