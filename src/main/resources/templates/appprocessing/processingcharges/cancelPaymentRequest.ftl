<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="pageRef" type="java.lang.String" -->
<#-- @ftlvariable name="appPaymentDisplaySummary" type="uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary" -->

<#include '../../layout.ftl'>

<#assign pageHeadingWithAppRef="${pageRef} ${appRef}" />

<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <h2 class="govuk-heading-l">${pageRef}</h2>

            <@fdsInsetText.insetText>
                Please note that by cancelling the payment, the application will require the Initial review to be re-completed.
            </@fdsInsetText.insetText>

            <@pwaPayment.applicationPaymentDisplaySummary summary=appPaymentDisplaySummary />

            <@fdsForm.htmlForm>
                <@fdsAction.submitButtons primaryButtonText="Cancel payment request" linkSecondaryAction=true secondaryLinkText="Back" linkSecondaryActionUrl=springUrl(cancelUrl)/>
            </@fdsForm.htmlForm>

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>