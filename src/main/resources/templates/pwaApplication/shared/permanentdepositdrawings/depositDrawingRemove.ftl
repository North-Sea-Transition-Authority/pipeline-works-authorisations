<#include '../../../layout.ftl'>
<#include 'depositsDrawingsViewSummary.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView" --> 


<@defaultPage htmlTitle="Remove permanent deposit drawing" pageHeading=("Are you sure you want to remove permanent deposit drawing " + depositDrawingView.reference + "?") breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>                
        <@depositDrawingViewSummary depositDrawingView depositDrawingUrlFactory/>     
        <@fdsAction.submitButtons primaryButtonText="Remove" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>