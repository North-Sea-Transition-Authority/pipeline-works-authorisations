<#include '../../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="consentDocumentUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory" -->

<@defaultPage htmlTitle="${appRef} - Reload document" breadcrumbs=false fullWidthColumn=true>

  <h1 class="govuk-heading-xl">
    Are you sure you want to reload the consent document for ${appRef}?
  </h1>

  <@fdsWarning.warning>
    This will clear all changes made to the document and recreate it from scratch.
  </@fdsWarning.warning>

  <@fdsForm.htmlForm>
    <@fdsAction.submitButtons primaryButtonText="Reload document" linkSecondaryAction=true secondaryLinkText="Don't reload" linkSecondaryActionUrl=springUrl(consentDocumentUrlFactory.renderEditorUrl)/>
  </@fdsForm.htmlForm>

</@defaultPage>