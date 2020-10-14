<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<@defaultPagePane htmlTitle=caseSummaryView.pwaApplicationRef phaseBanner=false>

    <@pwaAppSummary.summary
      pageHeading=""
      appSummaryView=appSummaryView
      sidebarHeading="Sections"
      caseSummaryView=caseSummaryView />

</@defaultPagePane>