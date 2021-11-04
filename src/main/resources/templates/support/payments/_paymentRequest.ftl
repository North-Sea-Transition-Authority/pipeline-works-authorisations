<#-- @ftlvariable name="paymentRequest" type="uk.co.ogauthority.pwa.features.pwapay.PwaPaymentRequest" -->
<#macro _pwaRequestData paymentRequest>
  <ul class="govuk-list">
    <li>PaymentUUID: ${paymentRequest.uuid}</li>
    <li>Reference: ${paymentRequest.reference}</li>
    <li>Description: ${paymentRequest.description!}</li>
    <li>Requested service: ${paymentRequest.requestedService}</li>
    <li>Payment request created: ${paymentRequest.createdTimestamp.toString()}</li>
    <li>Request status: ${paymentRequest.requestStatus}</li>
    <li>Request message: ${paymentRequest.requestStatusMessage!}</li>
    <li>Request status updated: ${paymentRequest.requestStatusTimestamp?has_content?then(paymentRequest.requestStatusTimestamp.toString(), "")}</li>
  </ul>
</#macro>

<#macro _govUkPayData paymentRequest>
  <ul class="govuk-list">
    <li>Payment ID: ${paymentRequest.govUkPaymentId!}</li>
    <li>Status: ${paymentRequest.govUkPaymentStatus!}</li>
    <li>Message: ${paymentRequest.govUkPaymentStatusMessage!}</li>
    <li>Status updated: ${paymentRequest.govUkPaymentStatusTimestamp?has_content?then(paymentRequest.govUkPaymentStatusTimestamp.toString(), "")}</li>
  </ul>
</#macro>