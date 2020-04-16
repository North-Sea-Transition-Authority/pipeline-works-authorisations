<#include '../../../layout.ftl'>


<@defaultPage htmlTitle="Add cable crossing" pageHeading="Add cable crossing" breadcrumbs=true>
    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.cableName" labelText="Name of the cable"/>
        <@fdsTextInput.textInput path="form.cableOwner" labelText="Name of cable owner"/>
        <@fdsTextarea.textarea path="form.location" labelText="Location of the cable" hintText="Include coordinates if known"/>
        <@fdsAction.button buttonText="Add cable crossing" />
    </@fdsForm.htmlForm>
</@defaultPage>