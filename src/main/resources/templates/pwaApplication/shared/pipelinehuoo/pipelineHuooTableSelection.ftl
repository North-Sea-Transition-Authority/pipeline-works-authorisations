<#include '../../../layout.ftl'>
<#import '/spring.ftl' as spring>

<#-- @ftlvariable name="pickableHuooPipelineOptions" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineOption>" -->


<!-- Macro based on the components/widgets/pipelineTableSelection.ftl template but customised for Pipeline huoo purposes -->
<#macro pickablePipelineTableSelection path pickableHuooPipelineOptions readOnlySelected=false caption="" captionClass="">
    <@spring.bind path/>
    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#local hasError=(spring.status.errorMessages?size > 0)>

  <div class="govuk-form-group <#if hasError> govuk-form-group--error</#if>">
      <#if hasError>
          <@fdsError.inputError inputId="${id}"/>
      </#if>

    <table id="${id}" class="govuk-table">
        <#if caption?has_content>
          <caption class="govuk-table__caption ${captionClass!""}">${caption}</caption>
        </#if>
      <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header ${readOnlySelected?then("govuk-visually-hidden", "")}" scope="col">
          <@pwaTableSelectionToggler.linksToggler tableId=id
            prefixText="Select"
            selectAllLinkText="All" selectAllScreenReaderText="Select all available pipelines"
            selectNoneLinkText="None" selectNoneScreenReaderText="Select none of the available pipelines" />
        </th>
        <th class="govuk-table__header govuk-!-width-one-third" scope="col" >Pipeline number / <br>Pipeline split details</th>
        <th class="govuk-table__header" scope="col">Pipeline type</th>
        <th class="govuk-table__header govuk-!-width-one-quarter" scope="col">Pipeline start</th>
        <th class="govuk-table__header govuk-!-width-one-quarter" scope="col">Pipeline end</th>
        <th class="govuk-table__header" scope="col">Length</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <#assign selectedItems = [] />
      <#list spring.stringStatusValue?split(",") as item>
          <#assign selectedItems = selectedItems + [item] />
      </#list>
      <#list pickableHuooPipelineOptions as pipeline>
          <#if pipeline?counter != 1>
              <#assign checkboxId = id + '-' + pipeline?counter/>
          <#else>
              <#assign checkboxId = id/>
          </#if>
          <#assign isSelected = selectedItems?seq_contains("${pipeline.pickableString}") />
          <#if ((readOnlySelected && isSelected) || !readOnlySelected)>
            <tr class="govuk-table__row">

              <td class="govuk-table__cell ${readOnlySelected?then("govuk-visually-hidden", "")}">
                <div class="govuk-checkboxes govuk-checkboxes--small">
                  <div class="govuk-checkboxes__item">
                    <input class="govuk-checkboxes__input" id="pipeline-checkbox-${checkboxId}"
                           name="${spring.status.expression}" type="checkbox" value="${pipeline.pickableString}"
                           <#if isSelected>checked</#if>>
                    <label class="govuk-label govuk-checkboxes__label" for="pipeline-checkbox-${checkboxId}"><span
                        class="govuk-visually-hidden">Select or de-select ${pipeline.pipelineNumber} ${pipeline.splitInfo?has_content?then("section " + pipeline.splitInfo, "")}</span>&nbsp;</label>
                  </div>
                </div>
              </td>

              <td class="govuk-table__cell"><ol class="govuk-list">
                  <li>${pipeline.pipelineNumber}</li>
                      <#if pipeline.splitInfo?has_content>
                        <li>${pipeline.splitInfo}</li>
                      </#if>
                </ol></td>
              <td class="govuk-table__cell">${pipeline.pipelineTypeDisplay}</td>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <li>${pipeline.getFromLocation()!}</li>
                    <#if pipeline.getFromCoordinates()?has_content>
                      <li><@pwaCoordinate.display pipeline.getFromCoordinates()/></li>
                    </#if>
                </ul>
              </td>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <li>${pipeline.getToLocation()!}</li>
                    <#if pipeline.getToCoordinates()?has_content>
                      <li><@pwaCoordinate.display pipeline.getToCoordinates()/></li>
                    </#if>
                </ul>
              </td>
              <td class="govuk-table__cell">${pipeline.length!}</td>
            </tr>
          </#if>
      </#list>
      </tbody>
    </table>
  </div>
</#macro>