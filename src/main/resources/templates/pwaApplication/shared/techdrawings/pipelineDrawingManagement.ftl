<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro pipelineDrawingManagement urlFactory pipelineDrawingSummaryViews=[]>
  <h2 class="govuk-heading-l">
    Pipeline drawings
  </h2>
    <#if pipelineDrawingSummaryViews?has_content>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Drawing reference</th>
          <th class="govuk-table__header" scope="col">Description</th>
          <th class="govuk-table__header" scope="col">Associated pipelines</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list pipelineDrawingSummaryViews as summary>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${summary.reference}</td>
            <td class="govuk-table__cell">${summary.documentDescription!""}</td>
            <td class="govuk-table__cell">
              <ul class="govuk-list govuk-list--bullet">
                <#list summary.pipelineOverviews as pipeline>
                    <li>${pipeline.pipelineNumber}</li>
                </#list>
              </ul>
            </td>
          </tr>
        </#list>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          No pipeline drawings have been added to this application
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>