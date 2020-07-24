<#include '../../../layout.ftl'>


<@defaultPage htmlTitle="Which crossing sections are required?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Which crossing sections are required?" legendHeadingSize="h1" legendHeadingClass="govuk-fieldset__legend--xl">
            <@fdsRadio.radioGroup path="form.pipelinesCrossed" labelText="Are any pipelines crossed?">
                <@fdsRadio.radioYes path="form.pipelinesCrossed"/>
                <@fdsRadio.radioNo path="form.pipelinesCrossed"/>
            </@fdsRadio.radioGroup>
            <@fdsRadio.radioGroup path="form.cablesCrossed" labelText="Are any cables crossed?">
                <@fdsRadio.radioYes path="form.cablesCrossed"/>
                <@fdsRadio.radioNo path="form.cablesCrossed"/>
            </@fdsRadio.radioGroup>
            <@fdsRadio.radioGroup path="form.medianLineCrossed" labelText="Is any median line crossed?">
                <@fdsRadio.radioYes path="form.medianLineCrossed"/>
                <@fdsRadio.radioNo path="form.medianLineCrossed"/>
            </@fdsRadio.radioGroup>
        </@fdsFieldset.fieldset>
        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>
    </@fdsForm.htmlForm>
</@defaultPage>