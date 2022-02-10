<#include '../../../layout.ftl'>
<#include 'depositsDrawingsViewSummary.ftl'>

<#-- @ftlvariable name="depositDrawingSummaryViews" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingView>" -->
<#-- @ftlvariable name="depositDrawingSummaryResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->


<@defaultPage htmlTitle="Permanent deposit drawings" breadcrumbs=true>

    <@validationResult.singleErrorSummary summaryValidationResult=depositDrawingSummaryResult! />
    <@validationResult.errorSummary summaryValidationResult=depositDrawingSummaryResult! />

    <h1 class="govuk-heading-xl">Permanent deposit drawings</h1>

    <@fdsForm.htmlForm>

        <@fdsAction.link linkText="Add drawing" linkUrl=springUrl(depositDrawingUrlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>

        <hr class="govuk-section-break govuk-section-break--m">

        <#list depositDrawingSummaryViews as depositDrawingView>
            <h2 class="govuk-heading-m">${depositDrawingView.reference}</h2>

            <#assign sectionId = validationResult.constructObjectId(depositDrawingSummaryResult!, depositDrawingView.depositDrawingId) />
            <#assign hasErrors = validationResult.hasErrors(depositDrawingSummaryResult!, sectionId) />
            <#assign sectionErrorMessage = validationResult.errorMessageOrEmptyString(depositDrawingSummaryResult!, sectionId) />
            <#if sectionErrorMessage?has_content>
                <span id=${sectionId} class="govuk-error-message">
                    <span class="govuk-visually-hidden">Error:</span> ${sectionErrorMessage}<br/>
                </span>
            </#if>

            <@fdsAction.link linkText="Edit" linkUrl=springUrl(depositDrawingUrlFactory.getEditDrawingUrl(depositDrawingView.depositDrawingId)) linkScreenReaderText="Edit ${depositDrawingView.reference}" linkClass="govuk-link govuk-!-font-size-19"/>&nbsp;
            <@fdsAction.link linkText="Remove" linkUrl=springUrl(depositDrawingUrlFactory.getRemoveDrawingUrl(depositDrawingView.depositDrawingId)) linkScreenReaderText="Remove ${depositDrawingView.reference}" linkClass="govuk-link govuk-!-font-size-19"/>
            <@depositDrawingViewSummary depositDrawingView depositDrawingUrlFactory/>
        </#list>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>