<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="pageRef" type="java.lang.String" -->
<#-- @ftlvariable name="appPaymentDisplaySummary" type="uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary" -->

<#include '../../layout.ftl'>

<#assign pageHeadingWithAppRef="${appRef} ${pageRef}" />

<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <h2 class="govuk-heading-l">${pageRef}</h2>

            <@pwaPayment.applicationPaymentDisplaySummary summary=appPaymentDisplaySummary />

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>