<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Field information" breadcrumbs=true>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.linkedToField" labelText="Is your application linked to a field?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l" hiddenContent=true>
            <@fdsRadio.radioYes path="form.linkedToField">
                <@fdsSelect.select path="form.fieldId" labelText="Field name" options=fieldMap nestingPath="form.fieldId"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.linkedToField"/>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText="Confirm" secondaryLinkText="Go back" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>