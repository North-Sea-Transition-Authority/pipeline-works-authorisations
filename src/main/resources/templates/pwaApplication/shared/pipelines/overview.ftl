<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineOverviews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->
<#-- @ftlvariable name="addPipelineUrl" type="String" -->

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
                    <@fdsTaskList.taskListItem itemText=task.taskName itemUrl=springUrl(task.route) completed=task.completed/>
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

</@defaultPage>