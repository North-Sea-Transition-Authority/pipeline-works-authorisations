<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Withdraw application" pageHeading="Withdraw application ${appRef}" topNavigation=true fullWidthColumn=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextarea.textarea path="form.withdrawalReason" labelText="Provide a reason for why you are withdrawing this application" characterCount=true maxCharacterLength="4000"/>        

        <@fdsAction.submitButtons primaryButtonText="Withdraw application" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>