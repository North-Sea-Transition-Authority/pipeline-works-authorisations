<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Project Information" pageHeading="Project information">
    <@fdsForm.htmlForm actionUrl="" useMethod="">
        <@fdsDateInput.dateInput dayPath="form.workStartDay" monthPath="form.workStartMonth" yearPath="form.workStartYear" formId="1" labelText="Estimated start date" fieldsetHeadingClass="govuk-fieldset__legend--l"/>

        <@fdsFieldset.fieldset legendHeading="Details" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2">
            <@fdsTextInput.textInput path="form.field" labelText="Field"/>
            <@fdsTextarea.textarea path="form.description" labelText="Project description" hintText="Please provide a brief description of your project"/>
        </@fdsFieldset.fieldset>

        <@fdsDateInput.dateInput dayPath="form.earliestCompletionDay" monthPath="form.earliestCompletionMonth" yearPath="form.earliestCompletionYear" formId="1" labelText="Earliest completion date" defaultHint=false fieldsetHeadingClass="govuk-fieldset__legend--l"/>
        <@fdsDateInput.dateInput dayPath="form.latestCompletionDay" monthPath="form.latestCompletionMonth" yearPath="form.latestCompletionYear" formId="1" labelText="Latest completion date" defaultHint=false fieldsetHeadingClass="govuk-fieldset__legend--l"/>
        <@fdsAction.submitButtons errorMessage="" linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>
</@defaultPage>