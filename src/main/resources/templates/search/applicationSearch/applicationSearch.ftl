<#include '../../layout.ftl'>
<#import '../../workarea/_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>
<#import '../common/searchScreenHelper.ftl' as searchScreenHelper>

<#-- @ftlvariable name="clearFiltersUrl" type="java.lang.String" -->

<#-- @ftlvariable name="searchScreenView" type="uk.co.ogauthority.pwa.model.view.search.SearchScreenView" -->
<#-- @ftlvariable name="appSearchEntryState" type="uk.co.ogauthority.pwa.controller.search.applicationsearch.ApplicationSearchController.AppSearchEntryState" -->
<#-- @ftlvariable name="searchUrl" type="java.lang.String" -->
<#-- @ftlvariable name="assignedCaseOfficers" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="pwaApplicationTypeMap" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="userTypes" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.users.UserType>" -->

<#-- @ftlvariable name="useLimitedOrgSearch" type="java.lang.Boolean" -->
<#-- @ftlvariable name="orgsRestUrl" type="java.lang.String" -->
<#-- @ftlvariable name="limitedOrgUnitOptions" type="java.util.Map<java.lang.String, java.lang.String>" -->

<@defaultPage htmlTitle="Search applications" pageHeading="Search applications" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <@fdsSearch.searchPage>

        <@fdsSearch.searchFilter formActionUrl="${springUrl(searchUrl)}">
            <@fdsSearch.searchFilterList filterButtonItemText="applications" clearFilterText="Clear filters" clearFilterUrl=springUrl(clearFiltersUrl)>
                <@fdsSearch.searchFilterItem itemName="Application reference" expanded=form.appReference?has_content>
                    <@fdsTextInput.textInput path="form.appReference"
                    labelText="Application reference" labelClass="govuk-visually-hidden"
                    hintText="Enter a full or partial reference"
                    maxCharacterLength="20" inputClass="govuk-input--width-10"
                    />
                </@fdsSearch.searchFilterItem>

                <@fdsSearch.searchFilterItem itemName="Application status" expanded=form.includeCompletedOrWithdrawnApps?has_content>
                    <@fdsCheckbox.checkboxGroup path="form.includeCompletedOrWithdrawnApps"
                    fieldsetHeadingText="Application status"
                    formGroupClass=""
                    smallCheckboxes=true
                    hiddenContent=false
                    inline=true
                    showLabelOnly=true
                    noFieldsetHeadingSize="--s govuk-visually-hidden">
                        <@fdsCheckbox.checkboxItem path="form.includeCompletedOrWithdrawnApps" labelText="Include Completed/Withdrawn applications"/>
                    </@fdsCheckbox.checkboxGroup>
                </@fdsSearch.searchFilterItem>

                <#if userTypes?seq_contains("OGA")>
                    <@fdsSearch.searchFilterItem itemName="Case officer" expanded=form.caseOfficerPersonId?has_content>
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.caseOfficerPersonId" options=assignedCaseOfficers labelText="Select a case officer" optionalInputDefault="Any" labelClass="govuk-visually-hidden" />
                    </@fdsSearch.searchFilterItem>
                </#if>
                
                <@fdsSearch.searchFilterItem itemName="Application type" expanded=form.pwaApplicationType?has_content>
                    <@fdsSearchSelector.searchSelectorEnhanced path="form.pwaApplicationType" options=pwaApplicationTypeMap labelText="Select an application type" optionalInputDefault="Any" labelClass="govuk-visually-hidden" />
                </@fdsSearch.searchFilterItem>

                <@fdsSearch.searchFilterItem itemName="Holder organisation" expanded=form.holderOrgUnitId?has_content>
                <#if useLimitedOrgSearch>
                    <@fdsSearchSelector.searchSelectorEnhanced path="form.holderOrgUnitId" options=limitedOrgUnitOptions labelText="Select an application type" optionalInputDefault="Any" labelClass="govuk-visually-hidden" />
                <#else>
                    <@fdsSearchSelector.searchSelectorRest path="form.holderOrgUnitId" preselectedItems=preselectedHolderOrgUnits restUrl=springUrl(orgsRestUrl) labelText="Select an organisation" optionalInputDefault="Any" labelClass="govuk-visually-hidden" />
                </#if>
                </@fdsSearch.searchFilterItem>

            </@fdsSearch.searchFilterList>
        </@fdsSearch.searchFilter>   

        <@fdsSearch.searchPageContent>

            <#if searchScreenView?has_content>

                <#if appSearchEntryState == "SEARCH" && !searchScreenView.searchResults?has_content>
                    <@searchScreenHelper.noResultsFound />
                </#if>

                <#if searchScreenView.searchResults?has_content>
                    <@searchScreenHelper.resultsCountText searchScreenView "search" />

                    <table class="govuk-table">
                        <thead class="govuk-table__head">
                        <tr class="govuk-table__row">
                            <th class="govuk-table__header govuk-!-width-one-third" scope="col">Application</th>
                            <th class="govuk-table__header" scope="col">Holder</th>
                            <th class="govuk-table__header govuk-!-width-one-third" scope="col">Summary</th>
                            <th class="govuk-table__header govuk-!-width-one-third" scope="col">Application status</th>
                        </tr>
                        </thead>
                        <tbody class="govuk-table__body">
                        <#list searchScreenView.searchResults as item>
                            <@applicationWorkAreaItem.workAreaItemContentRow item/>
                        </#list>
                        </tbody>
                    </table>
                </#if>

            </#if>

        </@fdsSearch.searchPageContent>

    </@fdsSearch.searchPage>

</@defaultPage>