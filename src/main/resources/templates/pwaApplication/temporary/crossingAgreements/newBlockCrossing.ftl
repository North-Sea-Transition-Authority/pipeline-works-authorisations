<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Add block crossing agreement" pageHeading="Add a new block crossing agreement" backLink=true>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Block crossing details">
            <@fdsTextInput.textInput path="form.blockNumber" labelText="Block number"/>
            <@fdsNumberInput.numberInputItem path="form.licenseNumber" labelText="License number" inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.button buttonText="Add block crossing"/>

    </@fdsForm.htmlForm>

</@defaultPage>