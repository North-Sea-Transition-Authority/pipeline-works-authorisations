<#include '../../../layoutPane.ftl'>

<#import 'consentDocumentEditorActions.ftl' as consentActions>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="docInstanceExists" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentDocumentUrlProvider" type="uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentDocumentUrlProvider" -->
<#-- @ftlvariable name="clauseActionsUrlProvider" type="uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlProvider" -->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission>" -->
<#-- @ftlvariable name="automaticMailMergePreviewClasses" type="String" -->
<#-- @ftlvariable name="manualMailMergePreviewClasses" type="String" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Prepare consent" />

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <#-- this link is used to provide the download url to docgenAutoDownloader.js -->
    <a id="doc-download-link" style="display:none" href="${springUrl(consentDocumentUrlProvider.downloadUrl)}"/>

    <@defaultPagePaneSubNav>
          <#if docView?has_content>
              <@fdsSubNavigation.subNavigation sticky=true>
                  <@pwaClauseList.sidebarSections documentView=docView />
              </@fdsSubNavigation.subNavigation>
          <#else>
              <@fdsSubNavigation.subNavigation sticky=true>
                  <@fdsSubNavigation.subNavigationSection themeHeading="No document loaded"/>
              </@fdsSubNavigation.subNavigation>
          </#if>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent breadcrumbs=true>

      <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

      <#if !docInstanceExists>

          <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlProvider.loadDocumentUrl)>
              <@fdsAction.button buttonText="Load document" />
          </@fdsForm.htmlForm>

      <#else>

          <@consentActions.actions userProcessingPermissions consentDocumentUrlProvider />

      </#if>

      <#if docView?has_content>

        <@fdsInsetText.insetText>
          <p>Information pulled in from the application is shown in <span class="${automaticMailMergePreviewClasses}">blue</span>.</p>
          <p>Phrases shown in <span class="${manualMailMergePreviewClasses}">red</span> must be edited before the consent can be sent for approval.</p>
        </@fdsInsetText.insetText>

        <@pwaClauseList.list documentView=docView clauseActionsUrlProvider=clauseActionsUrlProvider/>
      </#if>

    </@defaultPagePaneContent>

  <script src="${springUrl("/assets/static/js/pwa/docgenAutoDownloader.js")}"></script>

</@defaultPagePane>