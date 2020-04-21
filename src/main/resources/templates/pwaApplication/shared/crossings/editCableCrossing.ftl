<#include '../../../layout.ftl'>


<@defaultPage htmlTitle="Edit cable crossing" pageHeading="Add cable crossing" breadcrumbs=true>
    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.cableName" labelText="Name of the cable"/>
        <@fdsTextInput.textInput path="form.cableOwner" labelText="Name of cable owner"/>
        <@fdsTextarea.textarea path="form.location" labelText="Location of the cable" hintText="Include coordinates if known" maxCharacterLength="4000" characterCount=true/>
        <@fdsAction.button buttonText="Save cable crossing" />
    </@fdsForm.htmlForm>
</@defaultPage>