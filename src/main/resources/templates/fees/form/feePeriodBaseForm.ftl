<@fdsTextInput.textInput
inputClass="govuk-!-width-one-half"
path="form.periodDescription"
labelText="Fee period name"
/>

<@fdsDatePicker.datePicker
path="form.periodStartDate"
labelText="Select a date when the fee period is due to start"
labelClass="govuk-label--m"/>

<table class="govuk-table govuk-!-width-full">
  <thead class="govuk-table__head">
  <tr class="govuk-table__row">
    <th class="govuk-table__header " scope="col">Application type</th>
    <th class="govuk-table__header govuk-!-width-one-quarter" scope="col">Fee type</th>
    <th class="govuk-table__header govuk-!-width-two-thirds" scope="col">Cost</th>
  </tr>
  </thead>
  <tbody class="govuk-table__body">
  <#list applicationTypes as applicationType>
      <#list applicationFeeTypes as applicationFeeType>
        <tr class="govuk-table__row">
          <#if applicationFeeType_index == 0>
            <th  rowspan="2" class="govuk-table__cell">${applicationType.getDisplayName()}</th>
          </#if>
          <td class="govuk-table__cell">${applicationFeeType.getDisplayName()}</td>
          <td class="govuk-table__cell"><@fdsTextInput.textInput path="form.applicationCostMap[${applicationType}:${applicationFeeType}]"
              inputClass="govuk-input--width-7"
              formGroupClass="govuk-!-margin-bottom-0"
              labelText="Cost for ${applicationType.getDisplayName()}, ${applicationFeeType.getDisplayName()}"
              labelClass="govuk-visually-hidden"
              prefix="£"/></td>
        </tr>
      </#list>
  </#list>
  </tbody>
</table>