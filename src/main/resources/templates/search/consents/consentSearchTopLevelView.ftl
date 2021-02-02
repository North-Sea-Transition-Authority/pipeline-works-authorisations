<#-- @ftlvariable name="resultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->

<#macro topLevelData consentSearchView>

    <@fdsDataItems.dataItem dataItemListClasses="filter-result__data-list" >
      <@fdsDataItems.dataValues key="Holder" value=consentSearchView.holderNamesCsv!/>
      <@fdsDataItems.dataValues key="Field" value=consentSearchView.fieldNameOrOtherReference!/>
      <@fdsDataItems.dataValues key="Initial consent date" value=consentSearchView.firstConsentTimestampDisplay!/>
      <@fdsDataItems.dataValues key="Latest consent" value="${consentSearchView.latestConsentReference}<br/>${consentSearchView.latestConsentTimestampDisplay}"?no_esc/>
    </@fdsDataItems.dataItem>

</#macro>