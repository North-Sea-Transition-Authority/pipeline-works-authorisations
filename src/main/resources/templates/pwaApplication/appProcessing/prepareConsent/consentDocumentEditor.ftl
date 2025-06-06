<#include '../../../layoutPane.ftl'>
<#import '../../../consultation/consultationSosd.ftl' as consultationSosd>

<#import 'consentDocumentEditorActions.ftl' as consentActions>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="docInstanceExists" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentDocumentUrlProvider" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider" -->
<#-- @ftlvariable name="clauseActionsUrlProvider" type="uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlProvider" -->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission>" -->
<#-- @ftlvariable name="automaticMailMergePreviewClasses" type="String" -->
<#-- @ftlvariable name="manualMailMergePreviewClasses" type="String" -->
<#-- @ftlvariable name="sosdConsultationRequestView" type="java.util.List<"uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView>" -->
<#-- @ftlvariable name="openConsentReview" type="java.lang.Boolean" -->

<#assign pageHeading = "${caseSummaryView.pwaApplicationRef} - Prepare consent" />

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false backToTopLink=false>

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

          <@consentActions.actions userProcessingPermissions consentDocumentUrlProvider openConsentReview/>

      </#if>

      <@consultationSosd.sosdFileView sosdConsultationRequestView/>

      <#if docView?has_content>

        <@fdsInsetText.insetText>
          <p>Information pulled in from the application is shown in <span class="${automaticMailMergePreviewClasses}">green</span>.</p>
          <p>Phrases shown in <span class="${manualMailMergePreviewClasses}">red</span> are manual edit points that must be updated before the consent can be sent for approval.</p>
        </@fdsInsetText.insetText>

        <@pwaClauseList.list documentView=docView clauseActionsUrlProvider=clauseActionsUrlProvider/>
      </#if>
            
    </@defaultPagePaneContent>

  <script src="${springUrl("/assets/static/js/pwa/docgenAutoDownloader.js")}"></script>

</@defaultPagePane>