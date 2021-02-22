<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->

<#include '../../layout.ftl'>

<#assign pageHeading="Pay for application" />
<#assign pageHeadingWithAppRef="${pageHeading} ${appRef}" />

<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=true fullWidthColumn=true>
    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />
    <h2 class="govuk-heading-l">${pageHeading}</h2>

    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Start payment" linkSecondaryAction=true secondaryLinkText="Back" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>