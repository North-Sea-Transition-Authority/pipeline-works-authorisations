<#include '../../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="consentDocumentUrlProvider" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider" -->

<@defaultPage htmlTitle="${appRef} - Reload document" breadcrumbs=false fullWidthColumn=true>

  <h1 class="govuk-heading-xl">
    Are you sure you want to reload the consent document for ${appRef}?
  </h1>

  <@fdsWarning.warning>
    This will clear all changes made to the document and recreate it.
  </@fdsWarning.warning>

  <@fdsForm.htmlForm>
    <@fdsAction.submitButtons primaryButtonText="Reload document" primaryButtonClass="govuk-button govuk-button--warning" linkSecondaryAction=true secondaryLinkText="Don't reload" linkSecondaryActionUrl=springUrl(consentDocumentUrlProvider.renderEditorUrl)/>
  </@fdsForm.htmlForm>

</@defaultPage>