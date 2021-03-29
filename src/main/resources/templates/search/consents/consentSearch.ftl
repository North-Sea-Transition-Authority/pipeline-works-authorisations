<#include '../../layout.ftl'>
<#import 'consentSearchResultView.ftl' as consentSearchResultView>
<#import '../common/searchScreenHelper.ftl' as searchScreenHelper>

<#-- @ftlvariable name="searchScreenView" type="uk.co.ogauthority.pwa.model.view.search.SearchScreenView" -->
<#-- @ftlvariable name="searched" type="java.lang.Boolean" -->
<#-- @ftlvariable name="orgUnitFilterOptions" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="searchParams" type="uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams" -->
<#-- @ftlvariable name="consentSearchUrlFactory" type="uk.co.ogauthority.pwa.controller.search.consents.ConsentSearchUrlFactory" -->

<@defaultPage htmlTitle="Search PWAs" pageHeading="Search PWAs" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <@fdsSearch.searchPage>

        <@fdsSearch.searchFilter formActionUrl=springUrl(consentSearchUrlFactory.searchUrl)>
            <@fdsSearch.searchFilterList filterButtonItemText="PWAs" clearFilterText="Clear filters" clearFilterUrl=springUrl(consentSearchUrlFactory.searchUrl)>
                <@fdsSearch.searchFilterItem itemName="Holder organisation" expanded=searchParams.holderOrgUnitId?has_content>
                  <@fdsSearchSelector.searchSelectorEnhanced path="form.holderOuId" options=orgUnitFilterOptions labelText="Holder organisation" labelClass="govuk-visually-hidden" />
                </@fdsSearch.searchFilterItem>
                <@fdsSearch.searchFilterItem itemName="Consent reference" expanded=searchParams.consentReference?has_content>
                  <@fdsTextInput.textInput path="form.consentReference" labelText="Consent reference" labelClass="govuk-visually-hidden" />
                </@fdsSearch.searchFilterItem>
                <@fdsSearch.searchFilterItem itemName="Pipeline reference" expanded=searchParams.pipelineReference?has_content>
                  <@fdsTextInput.textInput path="form.pipelineReference" labelText="Pipeline reference" labelClass="govuk-visually-hidden" />
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
                      <@consentSearchResultView.view result consentSearchUrlFactory/>
                    </li>
                  </#list>
                </ol>
              </#if>

            </#if>

        </@fdsSearch.searchPageContent>
    </@fdsSearch.searchPage>

</@defaultPage>