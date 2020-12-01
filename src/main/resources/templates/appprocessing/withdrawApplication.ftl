<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Withdraw application" pageHeading="" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l">Withdraw application ${appRef}</h2>

    <@fdsForm.htmlForm>

        <@fdsTextarea.textarea path="form.withdrawalReason" labelText="Provide a reason for why you are withdrawing this application" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>        

        <@fdsAction.submitButtons primaryButtonText="Withdraw application" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>