<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Request application update" topNavigation=true breadcrumbs=true fullWidthColumn=true>

  <@fdsError.errorSummary errorItems=errorList />

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Request further information</h2>

  <@fdsForm.htmlForm>

    <@fdsTextarea.textarea path="form.requestReason" labelText="Why is an update required?" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>

    <@fdsAction.submitButtons primaryButtonText="Request application update" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>

</@defaultPage>