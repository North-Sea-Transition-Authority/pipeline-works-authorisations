<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="mappingGuidanceUrl" type="java.lang.String" -->

<@defaultPagePane htmlTitle=caseSummaryView.pwaApplicationRef phaseBanner=false>


    <#assign aboveSummaryInsert>
        <@fdsAction.link linkText="Download application pipeline map data" linkUrl=springUrl(mappingGuidanceUrl) openInNewTab=true linkClass="govuk-button govuk-button--blue"/>
    </#assign>

    <@pwaAppSummary.summary
      pageHeading=""
      appSummaryView=appSummaryView
      sidebarHeading="Sections"
      caseSummaryView=caseSummaryView
      aboveSummaryInsert=aboveSummaryInsert/>

</@defaultPagePane>