<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="docInstanceExists" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentDocumentUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory" -->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Consent document" />

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>
            <@pwaClauseList.sidebarSections documentView=docView />
        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent breadcrumbs=true>

      <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

      <#if !docInstanceExists>

          <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlFactory.loadDocumentUrl)>
              <@fdsAction.button buttonText="Load document" />
          </@fdsForm.htmlForm>

      <#else>

          <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlFactory.reloadDocumentUrl) linkClass="govuk-button govuk-button--blue" />

      </#if>

      <@pwaClauseList.list documentView=docView />

    </@defaultPagePaneContent>

</@defaultPagePane>