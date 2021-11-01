<#include '../../../layout.ftl'>

<#-- @ftlvariable name="workSchedule" type="uk.co.ogauthority.pwa.features.application.tasks.campaignworks.WorkScheduleView" -->

<#macro pipelineList workSchedule tableIdx="" >
    <#if workSchedule.getSchedulePipelines()?hasContent>
      <table id="work-schedule-pipelines-${tableIdx!""}" class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Pipeline number</th>
          <th class="govuk-table__header" scope="col">Pipeline type</th>
          <th class="govuk-table__header" scope="col">From</th>
          <th class="govuk-table__header" scope="col">To</th>
          <th class="govuk-table__header" scope="col">Length</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list workSchedule.schedulePipelines as pipeline>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${pipeline.pipelineNumber}</td>
            <td class="govuk-table__cell">${pipeline.pipelineTypeDisplayName}</td>
            <td class="govuk-table__cell">${pipeline.fromLocation}</td>
            <td class="govuk-table__cell">${pipeline.toLocation}</td>
            <td class="govuk-table__cell">${pipeline.metreLength}</td>
          </tr>
        </#list>
        </tbody>
      </table>
    <#else>
      <p class="govuk-body">No pipelines have been added to this work schedule</p>
    </#if>
</#macro>