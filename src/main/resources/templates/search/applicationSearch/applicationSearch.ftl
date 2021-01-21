<#include '../../layout.ftl'>
<#import '../../workarea/_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>
<#import '../common/searchScreenHelper.ftl' as searchScreenHelper>

<#-- @ftlvariable name="searchScreenView" type="uk.co.ogauthority.pwa.model.view.search.SearchScreenView" -->
<#-- @ftlvariable name="appSearchEntryState" type="uk.co.ogauthority.pwa.controller.search.applicationsearch.ApplicationSearchController.AppSearchEntryState" -->

<@defaultPage htmlTitle="Search applications" pageHeading="Search applications" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <@fdsInsetText.insetText>Search for submitted applications only, draft applications you are permitted to access are available in the work area.</@fdsInsetText.insetText>

    <@fdsForm.htmlForm>
        <@fdsAction.button buttonText="Search"/>
    </@fdsForm.htmlForm>

    <#if searchScreenView?has_content>

        <#if appSearchEntryState == "SEARCH" && !searchScreenView.searchResults?has_content>
            <@searchScreenHelper.noResultsFound />
        </#if>

        <@searchScreenHelper.resultsCountText searchScreenView "search" />

        <#if searchScreenView.searchResults?has_content>
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

</@defaultPage>