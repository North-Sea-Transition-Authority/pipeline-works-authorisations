<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineCards" type="java.util.List<uk.co.ogauthority.pwa.temp.model.view.PipelineCardView>" -->
<#-- @ftlvariable name="addProductionPipelineUrl" type="String" -->
<#-- @ftlvariable name="managePipelineOrgsUrl" type="String" -->
<#-- @ftlvariable name="viewEditPipelineUrl" type="String" -->
<#-- @ftlvariable name="saveCompleteLaterUrl" type="String" -->
<#-- @ftlvariable name="saveCompleteLaterUrl" type="String" -->

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines" breadcrumbs=true>

    <@fdsAction.link linkText="Add pipeline" linkUrl=springUrl(addProductionPipelineUrl) linkClass="govuk-button govuk-button--blue" />

    <@fdsAction.link linkText="Manage pipeline organisations" linkUrl=springUrl(managePipelineOrgsUrl) linkClass="govuk-button govuk-button--blue" />

    <#list pipelineCards as card>

        <@fdsCard.card>
            <@fdsCard.cardHeader cardHeadingText="${card.pipelineView.pipelineType.displayName} - ${card.pipelineView.pipelineNumber}">
                <@fdsCard.cardAction cardLinkText="View pipeline summary" cardLinkUrl=springUrl(card.pipelineOverviewUrl) />
            </@fdsCard.cardHeader>
            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValuesNumber key="Length" value="${card.pipelineView.length}m" valueId="${card.pipelineView.pipelineNumber}-length" />
                <@fdsDataItems.dataValues key="From" value="${card.pipelineView.from}" />
                <@fdsDataItems.dataValues key="To" value="${card.pipelineView.to}" />
                <@fdsDataItems.dataValues key="Products to be conveyed" value=card.pipelineView.productsToBeConveyed!"" />
            </@fdsDataItems.dataItem>



            <@fdsTaskList.taskList>

                <#list card.taskListEntryList as taskListEntry>
                  <@fdsTaskList.taskListItem itemText=taskListEntry.taskName itemUrl=springUrl(taskListEntry.taskRoute) completed=taskListEntry.completed/>
                </#list>

            </@fdsTaskList.taskList>
        </@fdsCard.card>

    </#list>

    <hr class="govuk-section-break govuk-section-break--xl">

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryLinkText="Save and continue later" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(saveCompleteLaterUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>