<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Transfer a pipeline from another PWA" pageHeading="Transfer a pipeline from another PWA" breadcrumbs=true fullWidthColumn=true>
    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorEnhanced
        path="form.pipelineId"
        options=claimablePipelines
        labelText="Select the pipeline to transfer to this PWA"
        labelClass="govuk-!-font-weight-bold"
        inputClass="govuk-!-width-one-third"/>

        <@fdsRadio.radioGroup path="form.assignNewPipelineNumber" labelText="Assign the pipeline a new pipeline number?">
            <@fdsRadio.radioYes path="form.assignNewPipelineNumber"/>
            <@fdsRadio.radioNo path="form.assignNewPipelineNumber"/>
        </@fdsRadio.radioGroup>

        <#if isCO2Pipeline>
          <@fdsDatePicker.datePicker path="form.lastIntelligentlyPigged" labelClass="govuk-!-font-weight-bold" labelText="When was the pipeline last intelligently pigged?"/>
          <@fdsRadio.radioGroup path="form.compatibleWithTarget" labelText="Are the pipeline materials compatible with Carbon dioxide?">
            <@fdsRadio.radioYes path="form.compatibleWithTarget"/>
            <@fdsRadio.radioNo path="form.compatibleWithTarget"/>
          </@fdsRadio.radioGroup>
        </#if>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>
