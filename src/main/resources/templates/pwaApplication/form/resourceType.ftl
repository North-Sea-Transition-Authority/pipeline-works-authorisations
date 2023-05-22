<#include '../../layout.ftl'>

<#-- @ftlvariable name="ouMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="ogList" type="java.util.List<String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="ogaServiceDeskEmail" type="String" -->

<@defaultPage htmlTitle="Application Type" errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsRadio.radio
        path="form.resourceType"
        radioItems=resourceOptionsMap
        labelText="What application type is this?"/>
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>
