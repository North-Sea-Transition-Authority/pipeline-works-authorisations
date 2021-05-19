<#macro mailMergeFieldList mergeFields>

  <table class="govuk-table" id="pwa-mail-merge-fields-list">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-three-quarters" scope="col">Mail merge field</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list mergeFields?sort as mergeField>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${mergeField}</td>
      </tr>
    </#list>
    </tbody>
  </table>

</#macro>