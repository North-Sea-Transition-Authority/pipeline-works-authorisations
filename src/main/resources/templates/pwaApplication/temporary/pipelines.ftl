<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.temp.model.view.PipelineView>" -->
<#-- @ftlvariable name="addProductionPipelineUrl" type="String" -->
<#-- @ftlvariable name="viewEditPipelineUrl" type="String" -->
<#-- @ftlvariable name="saveCompleteLaterUrl" type="String" -->

<@defaultPage htmlTitle="Pipelines" pageHeading="Pipelines">

    <@fdsAction.link linkText="Add production pipeline" linkUrl=springUrl(addProductionPipelineUrl) linkClass="govuk-button govuk-button--secondary" />

    <#list pipelineViews as pipeline>

        <@fdsCard.card>

            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineType.displayName} - ${pipeline.pipelineNumber}">
                <@fdsCard.cardAction cardLinkText="View or edit this pipeline" cardLinkUrl=springUrl(viewEditPipelineUrl + pipeline.pipelineNumber) />
            </@fdsCard.cardHeader>

            <#if pipeline.subPipelines?size gt 0>



            <#else>

                <hr class="govuk-section-break govuk-section-break--m">
                <p class="govuk-body">There are no pipelines linked to this one.</p>

            </#if>

        </@fdsCard.card>

    </#list>

    <hr class="govuk-section-break govuk-section-break--xl">

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryLinkText="Save and continue later" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(saveCompleteLaterUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>