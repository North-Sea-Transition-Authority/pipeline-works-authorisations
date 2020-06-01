<#include '../../../layout.ftl'>
<#include 'permanentDepositsViewSummary.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposits" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm>" --> 


<@defaultPage htmlTitle="Permanent deposits" pageHeading="Permanent deposits" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsInsetText.insetText>
            The Consent will only authorise deposits exactly as described, up to the maximum quantities specified to be laid, in the positions listed and within the period stated within the Table - nothing else can be laid.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add deposit" linkUrl=springUrl(addDepositUrl) linkClass="govuk-button govuk-button--blue"/>

        <#list deposits as deposit>
            <h2 class="govuk-heading-m">${deposit.depositReference}</h2>    
            <@fdsAction.link  linkText="Change" linkUrl=springUrl(editDepositUrls[deposit.entityID?string.number]) linkClass="govuk-link govuk-link--button" />
            <@fdsAction.link  linkText="Remove" linkUrl=springUrl(removeDepositUrls[deposit.entityID?string.number]) linkClass="govuk-link govuk-link--button" />
            <@depositViewSummary deposit/>
        </#list>      


        <@fdsAction.submitButtons errorMessage=errorMessage!"" primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>