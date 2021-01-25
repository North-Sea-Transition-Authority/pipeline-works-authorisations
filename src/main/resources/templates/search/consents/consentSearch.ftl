<#include '../../layout.ftl'>
<#import 'consentSearchResultView.ftl' as consentSearchResultView>
<#import '../common/searchScreenHelper.ftl' as searchScreenHelper>

<#-- @ftlvariable name="searchScreenView" type="uk.co.ogauthority.pwa.model.view.search.SearchScreenView" -->
<#-- @ftlvariable name="searched" type="java.lang.Boolean" -->
<#-- @ftlvariable name="orgUnitFilterOptions" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="searchParams" type="uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams" -->

<@defaultPage htmlTitle="Search PWAs" pageHeading="Search PWAs" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <@fdsSearch.searchPage>

        <@fdsSearch.searchFilter>
            <@fdsSearch.searchFilterList filterButtonItemText="PWAs">
                <@fdsSearch.searchFilterItem itemName="Holder organisation" expanded=searchParams.holderOrgUnitId?has_content>
                  <@fdsSearchSelector.searchSelectorEnhanced path="form.holderOuId" options=orgUnitFilterOptions labelText="Holder organisation" labelClass="govuk-visually-hidden" />
                </@fdsSearch.searchFilterItem>
            </@fdsSearch.searchFilterList>
        </@fdsSearch.searchFilter>

        <@fdsSearch.searchPageContent>

            <#if searchScreenView?has_content>

              <#if !searchScreenView.searchResults?has_content>
                <#if searched>
                  <@searchScreenHelper.noResultsFound />
                </#if>
              <#else>
                <@searchScreenHelper.resultsCountText searchScreenView "filter"/>
                <ol class="govuk-list filter-result-list">
                  <#list searchScreenView.searchResults as result>
                    <li class="govuk-list__item filter-result-list__item">
                      <@consentSearchResultView.view result />
                    </li>
                  </#list>
                </ol>
              </#if>

            </#if>

        </@fdsSearch.searchPageContent>
    </@fdsSearch.searchPage>

</@defaultPage>