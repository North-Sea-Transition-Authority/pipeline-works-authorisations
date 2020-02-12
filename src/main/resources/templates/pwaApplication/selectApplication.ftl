<#include '../layout.ftl'>

<#-- @ftlvariable name="createPwaApplicationUrl" type="String" -->
<#-- @ftlvariable name="workAreaUrl" type="String" -->
<#-- @ftlvariable name="applicationTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<@defaultPage htmlTitle="Start PWA application" pageHeading="">

    <@fdsError.errorSummary errorItems=errorList/>

    <@fdsForm.htmlForm>

        <@fdsRadio.radio
            path="form.applicationType"
            radioItems=applicationTypes
            labelText="What type of application do you want to start?"
            fieldsetHeadingClass="govuk-fieldset__legend--l"
            fieldsetHeadingSize="h1"/>

        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workAreaUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>