<#include '../../../layout.ftl'>

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

            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="Length" value="${pipeline.length}m" />
                <@fdsDataItems.dataValues key="From" value="${pipeline.fromLocation}" />
                <@fdsDataItems.dataValues key="To" value="${pipeline.toLocation}" />
                <@fdsDataItems.dataValues key="Component parts" value="${pipeline.componentParts}" />
                <@fdsDataItems.dataValues key="Products to be conveyed" value=pipeline.productsToBeConveyed />
            </@fdsDataItems.dataItem>

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