<#-- @ftlvariable name="resultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->

<#macro topLevelData consentSearchView>

    <@fdsDataItems.dataItem dataItemListClasses="filter-result__data-list" >
      <@fdsDataItems.dataValues key="Holder" value=consentSearchView.holderNamesCsv!/>
        <#if consentSearchView.resourceType.name() == "CCUS">
            <@fdsDataItems.dataValues key="Storage area" value=consentSearchView.fieldNameOrOtherReference!"No linked area"/>
        <#else>
            <@fdsDataItems.dataValues key="Field" value=consentSearchView.fieldNameOrOtherReference!"No linked field"/>
        </#if>

      <@fdsDataItems.dataValues key="Type" value=consentSearchView.resourceType.displayName!/>
      <@fdsDataItems.dataValues key="Initial consent date" value=consentSearchView.firstConsentTimestampDisplay!/>
      <@fdsDataItems.dataValues key="Latest consent" value="${consentSearchView.latestConsentReference}<br/>${consentSearchView.latestConsentTimestampDisplay}"?no_esc/>
    </@fdsDataItems.dataItem>

</#macro>
