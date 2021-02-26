<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary" -->

<#macro applicationPaymentDisplaySummary summary>
    <table class="govuk-table">
        <caption class="govuk-table__caption govuk-table__caption-m">${summary.headlineSummary}</caption>
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
            <th scope="col" class="govuk-table__header">Item</th>
            <th scope="col" class="govuk-table__header govuk-table__header--numeric">Cost</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
            <#list summary.displayableFeeItemList as displayableFee>
                <tr class="govuk-table__row">
                    <td class="govuk-table__cell ">${displayableFee.description}</td>
                    <td class="govuk-table__cell govuk-table__cell--numeric">&#163;${displayableFee.formattedAmount}</td>
                </tr
            </#list>
            <tr class="govuk-table__row">
                <th scope="row" class="govuk-table__header">Total charge</th>
                <th class="govuk-table__cell govuk-table__cell--numeric">&#163;${summary.formattedAmount}</th>
            </tr>
        </tbody>
    </table>
</#macro>