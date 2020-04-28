<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipelineOverviews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->
<#-- @ftlvariable name="addPipelineUrl" type="String" -->

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" fullWidthColumn=true>

    <#list pipelineOverviews as pipeline>

        <@fdsCard.card>

            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineNumber} ${pipeline.pipelineType.displayName}">
                <@fdsCard.cardAction cardLinkText="View pipeline summary" cardLinkUrl=springUrl("#") />
            </@fdsCard.cardHeader>

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

    <@fdsAction.link linkText="Add pipeline" linkUrl=springUrl(addPipelineUrl) linkClass="govuk-button govuk-button--blue" />

</@defaultPage>