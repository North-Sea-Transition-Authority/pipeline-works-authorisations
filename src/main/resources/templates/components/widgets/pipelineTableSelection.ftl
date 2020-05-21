<#include '../../layout.ftl'>
<#import '/spring.ftl' as spring>

<#macro pipelineTableSelection path pipelineOverviews>
    <@spring.bind path/>
    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#local hasError=(spring.status.errorMessages?size > 0)>

  <div class="govuk-form-group <#if hasError> govuk-form-group--error</#if>">
      <#if hasError>
          <@fdsError.inputError inputId="${id}"/>
      </#if>
    <table id="${id}" class="govuk-table">
      <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header" scope="col"></th>
        <th class="govuk-table__header" scope="col">Pipeline number</th>
        <th class="govuk-table__header" scope="col">Pipeline type</th>
        <th class="govuk-table__header" scope="col">From</th>
        <th class="govuk-table__header" scope="col">To</th>
        <th class="govuk-table__header" scope="col">Length</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <#assign selectedItems = [] />
      <#list spring.stringStatusValue?split(",") as item>
          <#assign selectedItems = selectedItems + [item] />
      </#list>
      <#list pipelineOverviews as pipeline>
          <#if pipeline?counter != 1>
              <#assign checkboxId = id + '-' + pipeline?counter>
          <#else>
              <#assign checkboxId = id>
          </#if>
          <#-- pipelineId must be converted to a string to check seq_contains. -->
          <#assign isSelected = selectedItems?seq_contains("${pipeline.pipelineId}")>
        <tr class="govuk-table__row">
          <td class="govuk-table__cell">
            <div class="govuk-checkboxes govuk-checkboxes--small">
              <div class="govuk-checkboxes__item">
                <input class="govuk-checkboxes__input" id="pipeline-checkbox-${checkboxId}" name="${spring.status.expression}" type="checkbox" value="${pipeline.pipelineId}" <#if isSelected>checked</#if>>
                <label class="govuk-label govuk-checkboxes__label" for="pipeline-checkbox-${checkboxId}"><span
                    class="govuk-visually-hidden">Select or de-select ${pipeline.pipelineNumber}</span>&nbsp;</label>
              </div>
            </div>
          </td>
          <td class="govuk-table__cell">${pipeline.pipelineNumber}</td>
          <td class="govuk-table__cell">${pipeline.pipelineType.displayName}</td>
          <td class="govuk-table__cell">${pipeline.fromLocation}</td>
          <td class="govuk-table__cell">${pipeline.toLocation}</td>
          <td class="govuk-table__cell">${pipeline.length}m</td>
        </tr>
      </#list>
      </tbody>
    </table>
  </div>
</#macro>