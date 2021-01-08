<#include '../../layout.ftl'>
<#import '../../workarea/_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>

<#-- @ftlvariable name="showMaxResultsExceededMessage" type="java.lang.Boolean" -->
<#-- @ftlvariable name="maxResults" type="java.lang.Long" -->
<#-- @ftlvariable name="displayableResults" type="type="java.util.List<uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem>" -->


<@defaultPage htmlTitle="Search applications" pageHeading="Search applications" fullWidthColumn=true topNavigation=true>

    <#if showMaxResultsExceededMessage>
        <@fdsWarning.warning>
            More than ${maxResults?c} applications have been found but only ${maxResults?c} are shown. Please refine your search criteria.
        </@fdsWarning.warning>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.button buttonText="Search"/>
    </@fdsForm.htmlForm>

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