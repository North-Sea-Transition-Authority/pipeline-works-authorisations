<#include '../../layout.ftl'>
<#import '../../workarea/_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>

<#-- @ftlvariable name="showMaxResultsExceededMessage" type="java.lang.Boolean" -->
<#-- @ftlvariable name="maxResults" type="java.lang.Long" -->
<#-- @ftlvariable name="displayableResults" type="type="java.util.List<uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem>" -->
<#-- @ftlvariable name="appSearchEntryState" type="uk.co.ogauthority.pwa.controller.search.applicationsearch.ApplicationSearchController.AppSearchEntryState" -->
<#-- @ftlvariable name="searchUrl" type="java.lang.String" -->


<@defaultPage htmlTitle="Search applications" pageHeading="Search applications" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <@fdsInsetText.insetText>Search for submitted applications only, draft applications you are permitted to access are available in the work area.</@fdsInsetText.insetText>

    <@fdsForm.htmlForm >
<#--        actionUrl="${springUrl(searchUrl)}"-->
        <@fdsTextInput.textInput path="form.appReference" labelText="Application reference" maxCharacterLength="10" inputClass="govuk-input--width-10"/>
        <@fdsAction.button buttonText="Search"/>
    </@fdsForm.htmlForm>

    <#if appSearchEntryState == "SEARCH" && !displayableResults?has_content>
    <h2 class="govuk-heading-s">There are no matching results</h2>
    <p class="govuk-body">Improve your results by:</p>
    <ul class="govuk-list govuk-list--bullet">
        <li>removing filters</li>
        <li>double-checking your spelling</li>
        <li>using fewer keywords</li>
        <li>searching for something less specific</li>
    </ul>
    </#if>

    <#if showMaxResultsExceededMessage>
        <@fdsWarning.warning>
            More than ${maxResults?c} applications have been found but only ${maxResults?c} are shown. Please refine your search criteria.
        </@fdsWarning.warning>
    </#if>

    <#if displayableResults?has_content>
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
            <#list displayableResults as item>
                <@applicationWorkAreaItem.workAreaItemContentRow item/>
            </#list>
            </tbody>
        </table>
    </#if>

</@defaultPage>