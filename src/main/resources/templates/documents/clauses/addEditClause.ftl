<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Add clause" pageHeading="Add clause" topNavigation=true twoThirdsColumn=true breadcrumbs=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.name" labelText="Clause name" hintText="This will be shown in the document sidebar" />

        <@fdsTextarea.textarea path="form.text" labelText="Clause text" />

        <@fdsAction.submitButtons primaryButtonText="Add clause" linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>