<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="financeRoleName" type="java.lang.String" -->
<#-- @ftlvariable name="paymentLandingPageUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appPaymentDisplaySummary" type="uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary" -->
<#-- @ftlvariable name="pwaHolderOrgNames" type="java.util.List<java.lang.String>" -->

<#include '../../layout.ftl'>

<#assign pageHeading="Pay for application" />
<#assign pageHeadingWithAppRef="${pageHeading} ${appRef}" />

<#assign sharePaymentUrlMailTo>mailto:?subject=Pay OGA for PWA application ${appRef}&body=Please use this link to pay the Oil and Gas Authority for our PWA application: ${paymentLandingPageUrl}</#assign>
<#assign linkclass="govuk-link govuk-link--no-visited-state"/>
<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=true fullWidthColumn=true>
    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <h2 class="govuk-heading-l">${pageHeading}</h2>

            <@fdsError.errorSummary errorItems=errorList />

            <@fdsInsetText.insetText>
                Please note that by starting the payment, any other person currently completing a payment for the application will have their attempt cancelled.
            </@fdsInsetText.insetText>
            <@fdsDetails.summaryDetails summaryTitle="Share this page for someone else to pay">
                <p>The person who pays for this application must either have permission to access this application, or be in the ${financeRoleName} role in the <@stringUtils.listToString list=pwaHolderOrgNames delimiter=" or "/> team.</p>
                <p>You can <@fdsAction.link linkText="send them an email with a link to pay" linkUrl=sharePaymentUrlMailTo linkClass=linkclass/> or copy this website address and send it to them: <@fdsAction.link linkText=paymentLandingPageUrl linkUrl=paymentLandingPageUrl linkClass=linkclass /></p>
            </@fdsDetails.summaryDetails>

            <@pwaPayment.applicationPaymentDisplaySummary summary=appPaymentDisplaySummary />

            <@fdsForm.htmlForm>
                <@fdsAction.submitButtons primaryButtonText="Start payment" linkSecondaryAction=true secondaryLinkText="Back" linkSecondaryActionUrl=springUrl(cancelUrl)/>
            </@fdsForm.htmlForm>

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>