<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Confirm satisfactory" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView showAppVersionNo=true/>

    <h2 class="govuk-heading-l">Confirm satisfactory application</h2>

    <@fdsForm.htmlForm>

        <@fdsTextarea.textarea
            path="form.reason"
            labelText="Reasons for confirming this version of the application is satisfactory"
            optionalLabel=true
            maxCharacterLength=maxCharacterLength?c
            characterCount=true
            inputClass="govuk-!-width-two-thirds"
        />

        <@fdsAction.submitButtons primaryButtonText="Confirm satisfactory" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>