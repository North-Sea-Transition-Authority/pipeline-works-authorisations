<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.temp.model.view.PipelineView>" -->
<#-- @ftlvariable name="addProductionPipelineUrl" type="String" -->
<#-- @ftlvariable name="viewEditPipelineUrl" type="String" -->
<#-- @ftlvariable name="saveCompleteLaterUrl" type="String" -->

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" breadcrumbs=true>

    <@fdsAction.link linkText="Add pipeline" linkUrl=springUrl(addProductionPipelineUrl) linkClass="govuk-button govuk-button--secondary" />

    <#list pipelineViews as pipeline>

        <@fdsCard.card>
            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineType.displayName} - ${pipeline.pipelineNumber}">
                <@fdsCard.cardAction cardLinkText="View pipeline summary" cardLinkUrl=springUrl(viewEditPipelineUrl + pipeline.pipelineNumber) />
            </@fdsCard.cardHeader>
            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValuesNumber key="Length" value="${pipeline.length}m" valueId="${pipeline.pipelineNumber}-length" />
                <@fdsDataItems.dataValues key="From" value="${pipeline.from}" />
                <@fdsDataItems.dataValues key="To" value="${pipeline.to}" />
                <@fdsDataItems.dataValues key="Products to be conveyed" value=pipeline.productsToBeConveyed!"" />
            </@fdsDataItems.dataItem>



            <@fdsTaskList.taskList>


                    <@fdsTaskList.taskListItem itemUrl=springUrl("#") itemText="Pipeline overview" completed=true/>
                    <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Idents"/>
                    <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Technical details"/>
                    <@fdsTaskList.taskListItem itemUrl=springUrl("#") itemText="Technical drawing"/>


            </@fdsTaskList.taskList>
        </@fdsCard.card>

    </#list>

  <hr class="govuk-section-break govuk-section-break--xl">

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryLinkText="Save and continue later" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(saveCompleteLaterUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>