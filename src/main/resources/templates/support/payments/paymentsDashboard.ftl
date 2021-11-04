<#include '../../layout.ftl'>
<#import '_paymentRequest.ftl' as paymentRequest>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="payments" type="uk.co.ogauthority.pwa.mvc.PageView<uk.co.ogauthority.pwa.features.webapp.devtools.paydashboard.PaymentRequestView>" -->
<#-- @ftlvariable name="startTestPaymentUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Pwa Payments dashboard" pageHeading="Pwa Payments dashboard" fullWidthColumn=true wrapperWidth=true errorItems=errorList>

    <@fdsForm.htmlForm actionUrl="${springUrl(startTestPaymentUrl)}">
      <@fdsTextInput.textInput path="form.amount" labelText="Penny amount"/>
        <@fdsTextInput.textInput path="form.reference" labelText="Reference"/>
        <@fdsAction.button buttonText="Create test payment"/>
    </@fdsForm.htmlForm>

    <#if !payments.getPageContent()?has_content>
      <@fdsInsetText.insetText>There are no recorded payment requests</@fdsInsetText.insetText>
    </#if>
    <@fdsPagination.pagination pageView=payments/>
    <#list payments.getPageContent() as paymentRequestView>
        <@fdsCard.card cardId="${paymentRequestView.paymentRequest.uuid}">
            <@fdsCard.cardHeader cardHeadingText="${paymentRequestView.paymentRequest.uuid}">

                <@fdsCard.cardAction cardLinkText="View/Update" cardLinkUrl="${springUrl(paymentRequestView.viewUrl)}" cardLinkScreenReaderText="${paymentRequestView.paymentRequest.uuid}"/>
            </@fdsCard.cardHeader>

            <@paymentRequest._pwaRequestData paymentRequestView.paymentRequest/>
        </@fdsCard.card>
    </#list>

    <@fdsPagination.pagination pageView=payments/>

</@defaultPage>