<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="docInstanceExists" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentDocumentUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory" -->
<#-- @ftlvariable name="clauseActionsUrlFactory" type="uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlFactory" -->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission>" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Consent document" />

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <#if docView?has_content>
      <@defaultPagePaneSubNav>
          <@fdsSubNavigation.subNavigation>
              <@pwaClauseList.sidebarSections documentView=docView />
          </@fdsSubNavigation.subNavigation>
      </@defaultPagePaneSubNav>
    </#if>

    <@defaultPagePaneContent breadcrumbs=true>

      <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

      <#if !docInstanceExists>

          <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlFactory.loadDocumentUrl)>
              <@fdsAction.button buttonText="Load document" />
          </@fdsForm.htmlForm>

      <#else>

          <#if userProcessingPermissions?seq_contains("SEND_CONSENT_FOR_APPROVAL")>
            <@fdsAction.link linkText="Send for approval" linkUrl=springUrl(consentDocumentUrlFactory.sendForApprovalUrl) linkClass="govuk-button govuk-button--green" />
          </#if>

          <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlFactory.reloadDocumentUrl) linkClass="govuk-button govuk-button--blue" />

          <@fdsAction.link linkText="Preview document" linkUrl=springUrl(consentDocumentUrlFactory.downloadUrl) linkClass="govuk-button govuk-button--secondary" />

      </#if>

      <#if docView?has_content>
        <@pwaClauseList.list documentView=docView clauseActionsUrlFactory=clauseActionsUrlFactory/>
      </#if>

    </@defaultPagePaneContent>

</@defaultPagePane>