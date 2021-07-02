<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="caseManagementUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="pageRef" type="java.lang.String" -->
<#-- @ftlvariable name="appPaymentDisplaySummary" type="uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary" -->
<#-- @ftlvariable name="paidByName" type="java.lang.String" -->
<#-- @ftlvariable name="paidByEmail" type="java.lang.String" -->
<#-- @ftlvariable name="paidInstant" type="java.lang.String" -->
<#-- @ftlvariable name="requestedInstant" type="java.lang.String" -->
<#-- @ftlvariable name="paymentStatus" type="java.lang.String" -->

<#include '../../layout.ftl'>

<#assign pageHeadingWithAppRef="${appRef} ${pageRef}" />

<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <h2 class="govuk-heading-l">${pageRef}</h2>

            <@fdsCheckAnswers.checkAnswers >
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Payment status">${paymentStatus}</@fdsCheckAnswers.checkAnswersRowNoAction>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Payment requested">${requestedInstant}</@fdsCheckAnswers.checkAnswersRowNoAction>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Paid by">${paidByName} (${paidByEmail})</@fdsCheckAnswers.checkAnswersRowNoAction>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Paid on">${paidInstant}</@fdsCheckAnswers.checkAnswersRowNoAction>
            </@fdsCheckAnswers.checkAnswers>

            <@pwaPayment.applicationPaymentDisplaySummary summary=appPaymentDisplaySummary />

            <@fdsAction.link linkText="Go back" linkUrl=springUrl(caseManagementUrl) linkClass="govuk-link govuk-link--stand-alone " linkScreenReaderText="Go back to previous page" role=false start=false/>

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>