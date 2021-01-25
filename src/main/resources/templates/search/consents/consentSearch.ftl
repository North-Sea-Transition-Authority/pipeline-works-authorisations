<#include '../../layout.ftl'>
<#import 'consentSearchResultView.ftl' as consentSearchResultView>

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
                  <h2 class="govuk-heading-s">There are no matching results</h2>
                  <p class="govuk-body">Improve your results by:</p>
                  <ul class="govuk-list govuk-list--bullet">
                    <li>removing filters</li>
                    <li>double-checking your spelling</li>
                    <li>using fewer keywords</li>
                    <li>searching for something less specific</li>
                  </ul>
                </#if>
              <#else>
                <h2 class="govuk-heading-s" role="alert">
                  <#if searchScreenView.resultsHaveBeenLimited()>
                    ${searchScreenView.fullResultCount?c} PWAs have been found but only ${searchScreenView.searchResults?size?c} are shown, you may need to refine your filter criteria.
                  <#else>
                    <@stringUtils.pluralise count=searchScreenView.searchResults?size word="PWA"/>
                  </#if>
                </h2>
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