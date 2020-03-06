<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.temp.model.view.PipelineView>" -->
<#-- @ftlvariable name="saveCompleteLaterUrl" type="String" -->

<@defaultPage htmlTitle="Link pipelines" pageHeading="Link pipelines to technical drawing" breadcrumbs=true>

  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">From</th>
      <th class="govuk-table__header" scope="col">To</th>
      <th class="govuk-table__header" scope="col">Products</th>
      <th class="govuk-table__header" scope="col">Selected</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list pipelineViews as hash, pipeline>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${pipeline.from} ${pipeline.pipelineNumber}</td>
        <td class="govuk-table__cell">${pipeline.from} ${pipeline.getFromLatString()?no_esc} ${pipeline.getFromLongString()?no_esc}</td>
        <td class="govuk-table__cell">${pipeline.to} ${pipeline.getToLatString()?no_esc} ${pipeline.getToLongString()?no_esc}</td>
        <td class="govuk-table__cell">${pipeline.productsToBeConveyed}</td>
        <td class="govuk-table__cell">
            <@fdsCheckbox.checkbox path="form.pipelineViews" labelText="" smallCheckboxes=true/>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <hr class="govuk-section-break govuk-section-break--xl">

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Update linked pipelines" secondaryLinkText="Cancel changes" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(saveCompleteLaterUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>