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
                <@fdsCard.cardAction cardLinkText="View or edit this pipeline" cardLinkUrl=springUrl(viewEditPipelineUrl + pipeline.pipelineNumber) />
            </@fdsCard.cardHeader>
            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="From" value="${pipeline.from} ${pipeline.getFromLatString()?no_esc} ${pipeline.getFromLongString()?no_esc}" />
                <@fdsDataItems.dataValues key="To" value="${pipeline.to} ${pipeline.getToLatString()?no_esc} ${pipeline.getToLongString()?no_esc}" />
                <@fdsDataItems.dataValues key="Products to be conveyed" value=pipeline.productsToBeConveyed />
            </@fdsDataItems.dataItem>
        </@fdsCard.card>

    </#list>

    <hr class="govuk-section-break govuk-section-break--xl">

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryLinkText="Save and continue later" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(saveCompleteLaterUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>