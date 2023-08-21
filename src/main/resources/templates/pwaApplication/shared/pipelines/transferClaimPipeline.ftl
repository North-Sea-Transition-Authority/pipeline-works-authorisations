<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Transfer a pipeline from another PWA" pageHeading="Transfer a pipeline from another PWA" breadcrumbs=true fullWidthColumn=true>
    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorEnhanced
        path="form.padPipelineId"
        options=claimablePipelines
        labelText="Select the pipeline to transfer to this PWA"/>

        <@fdsCheckbox.checkbox
        fieldsetHeadingText="Assign the pipeline a new pipeline number?"
        path="form.assignNewPipelineNumber"
        labelText="Assign a new pipeline number"/>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>