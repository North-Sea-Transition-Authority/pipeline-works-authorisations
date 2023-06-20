<#include '../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#macro summary caseSummaryView showAppSummaryLink=true showAppVersionNo=false>

  <span class="govuk-caption-l">${caseSummaryView.pwaApplicationTypeDisplay}</span>
  <h1 id=${caseSummaryView.caseSummaryHeaderId} class="govuk-heading-xl">${caseSummaryView.pwaApplicationRef} ${showAppVersionNo?then(" - Version " + caseSummaryView.versionNo, "")}
    <#if showAppSummaryLink>
      <br/>
      <@fdsAction.link
        linkText="View application (in new tab)"
        linkUrl=springUrl(caseSummaryView.getAppSummaryUrl())
        linkClass="govuk-link govuk-!-font-size-19 govuk-link--no-visited-state govuk-link--case-management-heading"
        openInNewTab=true />
    </#if>
  </h1>

  <#assign fastTrackText = caseSummaryView.fastTrackFlag?then(" (Fast track)", "") />

  <@fdsDataItems.dataItem>
      <@fdsDataItems.dataValues key="Holders" value=caseSummaryView.holderNames />
      <@fdsDataItems.dataValues key="Proposed start date" value=caseSummaryView.proposedStartDateDisplay!"" + fastTrackText />
      <@fdsDataItems.dataValues key="Fields" value=caseSummaryView.fieldNames!"Not linked to field" />
      <@fdsDataItems.dataValues key="Case officer" value=caseSummaryView.caseOfficerName!"Not yet assigned" />
      <#if caseSummaryView.masterPwaReference?has_content && caseSummaryView.getViewMasterPwaUrlIfVariation()?has_content>
          <#assign masterPwaLink>
            <@fdsAction.link
            linkUrl=springUrl(caseSummaryView.getViewMasterPwaUrlIfVariation())
            linkText="${caseSummaryView.masterPwaReference} (in new tab)"
            openInNewTab=true/>
          </#assign>

          <@fdsDataItems.dataValues key="PWA reference" value=masterPwaLink />
      </#if>
  </@fdsDataItems.dataItem>

</#macro>