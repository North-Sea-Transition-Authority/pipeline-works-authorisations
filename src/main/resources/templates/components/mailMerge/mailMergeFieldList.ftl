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

<#macro manualMergeGuidance showHeading=true>
  <#if showHeading><h3 class="govuk-heading-m">What does '??' mean?</h3></#if>
  <p class="govuk-body">Phrases starting and ending with '??' indicate that an edit needs to be made to remove text that does not apply to this application or PWA.</p>
</#macro>