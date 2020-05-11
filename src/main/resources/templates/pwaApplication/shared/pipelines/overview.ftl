<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineOverviews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->
<#-- @ftlvariable name="addPipelineUrl" type="String" -->
<#-- @ftlvariable name="errorMessage" type="String" -->

<#assign addPipeButton>
    <@fdsAction.link linkText="Add pipeline" linkUrl=springUrl(addPipelineUrl) linkClass="govuk-button govuk-button--blue" />
</#assign>

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" fullWidthColumn=true breadcrumbs=true>

    ${addPipeButton}

    <#list pipelineOverviews as pipeline>

        <@fdsCard.card>

            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineNumber} ${pipeline.pipelineType.displayName}" />

            <@pipelineOverview.header pipeline=pipeline />

            <@fdsTaskList.taskList>
                <#list pipeline.tasks as task>
                    <#if task.taskInfo?has_content>
                        <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfo=task.taskInfo/>
                    <#else>
                        <@fdsTaskList.taskListItem itemText=task.taskName itemUrl=springUrl(task.route) completed=task.completed/>
                    </#if>
                </#list>
            </@fdsTaskList.taskList>
        </@fdsCard.card>

    </#list>

    <#if pipelineOverviews?size gt 4>
      ${addPipeButton}
    </#if>

    <#if !pipelineOverviews?has_content>
      <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    <hr class="govuk-section-break govuk-section-break--l"/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later" errorMessage=errorMessage!/>
    </@fdsForm.htmlForm>

</@defaultPage>