<#include '../../../layout.ftl'>
<#include 'permanentDepositsViewSummary.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm" -->


<@defaultPage htmlTitle="Remove permanent deposit" pageHeading=("Are you sure you want to remove permanent deposit " + deposit.depositReference + "?") breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@depositViewSummary deposit/>
        <@fdsAction.submitButtons primaryButtonText="Remove" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>