<#include '../../layout.ftl'>
<#import '_paymentRequest.ftl' as paymentRequest>

<#-- @ftlvariable name="pwaPaymentRequest" type="uk.co.ogauthority.pwa.pwapay.PwaPaymentRequest" -->
<#-- @ftlvariable name="paymentsDashboardUrl" type="java.lang.String" -->
<#-- @ftlvariable name="paymentActionUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="View payment" pageHeading="View payment" fullWidthColumn=true wrapperWidth=true>

        <h2 class="govuk-heading govuk-heading-m">Pwa payment request</h2>
        <@fdsForm.htmlForm actionUrl="${springUrl(paymentActionUrl)}">
            <@fdsAction.button buttonText="Cancel payment" buttonName="action" buttonValue="CANCEL" />
            <@fdsAction.button buttonText="Refresh payment data" buttonName="action" buttonValue="REFRESH"/>
            <@fdsAction.link linkText="Back to payments dashboard" linkUrl="${springUrl(paymentsDashboardUrl)}" linkClass="govuk-link govuk-link--button" />
        </@fdsForm.htmlForm>

        <h3 class="govuk-heading govuk-heading-s">PWA request data</h3>
        <@paymentRequest._pwaRequestData paymentRequest=pwaPaymentRequest/>
        <h3 class="govuk-heading govuk-heading-s">Gov UK Pay request data</h3>
       <@paymentRequest._govUkPayData paymentRequest=pwaPaymentRequest/>

</@defaultPage>