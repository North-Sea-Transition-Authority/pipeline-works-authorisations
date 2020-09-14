<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="docInstanceExists" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentDocumentUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory" -->

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} - Consent document" breadcrumbs=true fullWidthColumn=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Consent document</h2>

  <#if !docInstanceExists>

    <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlFactory.loadDocumentUrl)>
        <@fdsAction.button buttonText="Load document" />
    </@fdsForm.htmlForm>

    <#else>

      <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlFactory.reloadDocumentUrl) linkClass="govuk-button govuk-button--blue" />

  </#if>

</@defaultPage>