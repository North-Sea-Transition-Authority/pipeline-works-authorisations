<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="Edit cable crossing" pageHeading="Edit cable crossing" breadcrumbs=true>
    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.cableName" labelText="Name of the cable"/>
        <@fdsTextInput.textInput path="form.cableOwner" labelText="Name of cable owner"/>
        <@fdsTextarea.textarea path="form.location" labelText="Location of the cable" hintText="Include coordinates if known" maxCharacterLength="4000" characterCount=true/>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Save cable crossing" secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
        <@fdsAction.button buttonText="Save cable crossing" />
    </@fdsForm.htmlForm>
</@defaultPage>