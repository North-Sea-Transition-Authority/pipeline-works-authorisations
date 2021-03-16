<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="actionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="${actionType.actionText} clause" pageHeading="${actionType.actionText} clause" topNavigation=true twoThirdsColumn=true breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.name" labelText="Clause name" hintText="This will be shown in the document sidebar" />

        <@fdsTextarea.textarea path="form.text" labelText="Clause text" />

        <@fdsAction.submitButtons primaryButtonText="${actionType.submitButtonText} clause" linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>