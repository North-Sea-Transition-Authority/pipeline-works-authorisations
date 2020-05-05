<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="Remove pipeline crossing" pageHeading="Remove pipeline crossing" breadcrumbs=true>

    <table class="govuk-table">
        <caption class="govuk-table__caption govuk-visually-hidden">Are you sure you want to remove this pipeline crossing?</caption>
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="row">Pipeline reference</th>
            <td class="govuk-table__cell">
                ${view.reference}
            </td>
        </tr>
        <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="row">Pipeline owners</th>
            <td class="govuk-table__cell">
                ${view.owners}
            </td>
        </tr>
        </tbody>
    </table>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline crossing" secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>