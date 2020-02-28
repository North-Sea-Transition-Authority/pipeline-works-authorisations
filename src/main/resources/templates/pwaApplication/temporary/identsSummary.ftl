<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineView" type="uk.co.ogauthority.pwa.temp.model.view.PipelineView" -->

<#macro identsSummary pipelineView canEdit=false>
    <h2 class="govuk-heading-l">Idents</h2>
    <#if canEdit>
        <@fdsAction.link linkText="Add ident" linkClass="govuk-button govuk-button--secondary" linkUrl=springUrl(addIdentUrl) />
    </#if>
    <#if !(pipelineView.idents?size gt 0)>
      <p class="govuk-body">No idents have been added</p>
    </#if>
    <#if pipelineView.idents?size gt 0>
      <table class="govuk-table">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Ident no.</th>
            <#list pipelineView.idents as ident>
              <th class="govuk-table__header govuk-table__header">${ident.identNo!""}</th>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">From</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">${ident.from!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">To</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">${ident.to!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Component parts</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">${ident.componentParts!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Type of insulation/coating</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">${ident.typeOfInsulationOrCoating!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Products to be conveyed</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">${ident.productsToBeConveyed!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Length (m)</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell govuk-table__cell">${ident.length!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">External diameter (mm)</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell govuk-table__cell">${ident.externalDiameter!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Internal diameter (mm)</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell govuk-table__cell">${ident.internalDiameter!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Wall thickness (mm)</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell govuk-table__cell">${ident.wallThickness!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">MAOP (Barg)</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell govuk-table__cell">${ident.maop!""}</td>
            </#list>
        </tr>
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="row">Actions</th>
            <#list pipelineView.idents as ident>
              <td class="govuk-table__cell">
                  <@fdsAction.link linkUrl="#" linkText="Edit" />
                <hr class="govuk-section-break"/>
                  <@fdsAction.link linkUrl="#" linkText="Remove" />
              </td>
            </#list>
        </tr>
        </tbody>
      </table>
    </#if>
</#macro>