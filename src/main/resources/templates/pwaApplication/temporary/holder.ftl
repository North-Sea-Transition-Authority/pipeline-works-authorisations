<#include '../../layout.ftl'>

<#-- @ftlvariable name="ouMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="backUrl" type="String" -->

<@defaultPage htmlTitle="Consent holder" pageHeading="Consent holder">

    <@fdsInsetText.insetText>Consent holders are...</@fdsInsetText.insetText>

    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>

        <@fdsSelect.select path="form.holderOuId" labelText="" options=ouMap />

        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(backUrl) />

    </@fdsForm.htmlForm>

</@defaultPage>