<#include '../../../layout.ftl'>
<#include 'permanentDepositsViewSummary.ftl'>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="deposits" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm>" -->
<#-- @ftlvariable name="depositSummaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->



<@defaultPage htmlTitle="Permanent deposits" breadcrumbs=true>

    <@validationResult.singleErrorSummary summaryValidationResult=depositSummaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=depositSummaryValidationResult! />

    <h1 class="govuk-heading-xl">Permanent deposits</h1>

    <@fdsForm.htmlForm>

        <@fdsInsetText.insetText>
            The Consent will only authorise deposits exactly as described, up to the maximum quantities specified to be laid, in the positions listed and within the period stated within the Table - nothing else can be laid.
        </@fdsInsetText.insetText>

        <@fdsAction.link linkText="Add deposit" linkUrl=springUrl(addDepositUrl) linkClass="govuk-button govuk-button--blue"/>

        <hr class="govuk-section-break govuk-section-break--s">

        <#list deposits as deposit>
            <h2 class="govuk-heading-m">${deposit.depositReference}</h2>

            <#assign sectionId = validationResult.constructObjectId(depositSummaryValidationResult!, deposit.entityID) />
            <#assign hasErrors = validationResult.hasErrors(depositSummaryValidationResult!, sectionId) />
            <#assign sectionErrorMessage = validationResult.errorMessageOrEmptyString(depositSummaryValidationResult!, sectionId) />
            <#if sectionErrorMessage?has_content>
                <span id=${sectionId} class="govuk-error-message">
                    <span class="govuk-visually-hidden">Error:</span> ${sectionErrorMessage}<br/>
                </span>
            </#if>

            <!-- To avoid putting the actions within the summary macro recreate the fds pattern using a standard list with small padding. -->
            <ol class="govuk-list govuk-!-margin-bottom-1">
                <li class="govuk-list__item govuk-list__item--inline"><@fdsAction.link linkText="Edit" linkUrl=springUrl(editDepositUrls[deposit.entityID?string.number]) linkScreenReaderText="${deposit.depositReference}" linkClass="govuk-link" /></li>
                <li class="govuk-list__item govuk-list__item--inline"><@fdsAction.link linkText="Remove" linkUrl=springUrl(removeDepositUrls[deposit.entityID?string.number]) linkScreenReaderText="${deposit.depositReference}" linkClass="govuk-link" /></li>
            </ol>

            <@depositViewSummary deposit=deposit/>
        </#list>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>