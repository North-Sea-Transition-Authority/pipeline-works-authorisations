<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Transfer a pipeline from another PWA" pageHeading="Transfer a pipeline from another PWA" breadcrumbs=true fullWidthColumn=true>
    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorEnhanced
        path="form.pipelineId"
        options=claimablePipelines
        labelText="Select the pipeline to transfer to this PWA"
        inputClass="govuk-!-width-one-third"/>

        <@fdsRadio.radioGroup path="form.assignNewPipelineNumber" labelText="Assign the pipeline a new pipeline number?">
            <@fdsRadio.radioYes path="form.assignNewPipelineNumber"/>
            <@fdsRadio.radioNo path="form.assignNewPipelineNumber"/>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>
