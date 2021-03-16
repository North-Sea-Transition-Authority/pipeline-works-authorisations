<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Add cable crossing" pageHeading="Add cable crossing" breadcrumbs=true errorItems=errorList>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.cableName" labelText="Name of the cable"/>
        <@fdsTextInput.textInput path="form.cableOwner" labelText="Name of cable owner"/>
        <@fdsTextarea.textarea path="form.location" labelText="Location of the cable" hintText="Include coordinates if known" maxCharacterLength="4000" characterCount=true/>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add cable crossing" secondaryLinkText="Back to cable crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>