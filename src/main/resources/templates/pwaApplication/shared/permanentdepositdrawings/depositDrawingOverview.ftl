<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 


<@defaultPage htmlTitle="Permanent deposits drawings" pageHeading="Permanent deposits drawings" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        
        <@fdsAction.link linkText="Add drawing" linkUrl=springUrl(addDrawingUrl) linkClass="govuk-button govuk-button--blue"/>        


        <@fdsAction.submitButtons errorMessage=errorMessage!"" primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>