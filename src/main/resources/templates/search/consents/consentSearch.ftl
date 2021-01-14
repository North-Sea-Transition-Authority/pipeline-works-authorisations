<#include '../../layout.ftl'>
<#import 'consentSearchResultView.ftl' as consentSearchResultView>

<#-- @ftlvariable name="resultsHaveBeenLimited" type="java.lang.Boolean" -->
<#-- @ftlvariable name="searched" type="java.lang.Boolean" -->
<#-- @ftlvariable name="maxResultsSize" type="java.lang.Boolean" -->
<#-- @ftlvariable name="searchResults" type="java.util.List<uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView>" -->

<@defaultPage htmlTitle="Search PWAs" pageHeading="Search PWAs" fullWidthColumn=true topNavigation=true>

    <@fdsSearch.searchPage>

        <@fdsSearch.searchFilter>
            <@fdsSearch.searchFilterList filterButtonItemText="PWAs">
                <@fdsSearch.searchFilterItem itemName="Holder organisation">
                  <@fdsSearch.searchTextInput path="form.holderOrganisation" labelText="Organisation name"/>
                </@fdsSearch.searchFilterItem>
            </@fdsSearch.searchFilterList>
        </@fdsSearch.searchFilter>

        <@fdsSearch.searchPageContent>

            <#if !searchResults?has_content>
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
              <h2 class="govuk-heading-s">
                <#if resultsHaveBeenLimited>
                  More than ${maxResultsSize?c} PWAs have been found but only ${maxResultsSize?c} are shown, you may need to refine your filter criteria.
                <#else>
                  ${searchResults?size} PWAs
                </#if>
              </h2>
              <ol class="govuk-list filter-result-list">
                <#list searchResults as result>
                  <li class="govuk-list__item filter-result-list__item">
                    <@consentSearchResultView.view result />
                  </li>
                </#list>
              </ol>
            </#if>

        </@fdsSearch.searchPageContent>
    </@fdsSearch.searchPage>

</@defaultPage>