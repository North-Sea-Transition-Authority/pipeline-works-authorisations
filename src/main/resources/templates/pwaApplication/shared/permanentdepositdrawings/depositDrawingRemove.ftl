<#include '../../../layout.ftl'>
<#include 'depositsDrawingsViewSummary.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="depositDrawingView" type="uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingView" -->
<#-- @ftlvariable name="depositDrawingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingUrlFactory" -->


<@defaultPage htmlTitle="Remove permanent deposit drawing" pageHeading=("Are you sure you want to remove permanent deposit drawing " + depositDrawingView.reference + "?") breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@depositDrawingViewSummary depositDrawingView depositDrawingUrlFactory/>
        <@fdsAction.submitButtons primaryButtonText="Remove" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>