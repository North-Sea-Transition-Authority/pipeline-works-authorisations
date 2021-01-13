<#include '../../layout.ftl'>
<#-- @ftlvariable name="resultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->
<#macro view resultView>

  <div class="filter-result">
    <h3 class="filter-result__heading">
        <@fdsAction.link
          linkText=resultView.pwaReference
          linkUrl="#" <#-- TODO PWA-1032 link to view page -->
          linkClass="govuk-link govuk-!-font-size-24 govuk-link--no-visited-state" />
    </h3>
    <@fdsDataItems.dataItem dataItemListClasses="filter-result__data-list" >
      <@fdsDataItems.dataValues key="Holder" value=resultView.holderNamesCsv!/>
      <@fdsDataItems.dataValues key="Field" value=resultView.fieldNameOrOtherReference!/>
      <@fdsDataItems.dataValues key="Initial consent date" value=resultView.firstConsentTimestampDisplay!/>
      <@fdsDataItems.dataValues key="Latest consent" value="${resultView.latestConsentReference}<br/>${resultView.latestConsentTimestampDisplay}"?no_esc/>
    </@fdsDataItems.dataItem>
  </div>

</#macro>