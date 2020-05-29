<#include '../../../layout.ftl'>
<#include 'permanentDepositsViewSummary.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm" --> 


<@defaultPage htmlTitle="Remove permanent deposit" pageHeading=("Remove permanent deposit " + deposit.depositReference) breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>                
        <@depositViewSummary deposit 0/>     
        <@fdsAction.submitButtons primaryButtonText="Remove" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>