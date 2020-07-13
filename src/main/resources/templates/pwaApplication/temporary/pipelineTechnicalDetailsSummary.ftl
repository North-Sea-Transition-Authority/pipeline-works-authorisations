<#include '../../layout.ftl'>

<#-- @ftlvariable name="technicalDetailsView" type="uk.co.ogauthority.pwa.temp.model.view.TechnicalDetailsView" -->

<#macro techDetailsSummary technicalDetailsView canEdit=false buttonClass="">
  <hr class="govuk-section-break govuk-section-break--m"/>
  <h2 class="govuk-heading-l">Technical details</h2>
  <#if canEdit>
      <@fdsAction.link linkText="Edit technical details" linkClass="govuk-button ${buttonClass}" linkUrl=springUrl("/") />
  </#if>
  <h3 class="govuk-heading-m">Pipeline design</h3>
  <table class="govuk-table">
    <tbody class="govuk-table__body">
    <tr class="govuk-table__row">
      <th class="govuk-table__header  govuk-!-width-one-third">Pipeline design statement</th>
      <td class="govuk-table__cell">${technicalDetailsView.generalStatementOfPipelineDesign}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Design life of pipeline</th>
      <td class="govuk-table__cell">
          <#if technicalDetailsView.designLifeSpanYears?has_content>${technicalDetailsView.designLifeSpanYears} years</#if>
      </td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Corrosion Management Strategy</th>
      <td class="govuk-table__cell">${technicalDetailsView.corrosionManagementStrategy}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Trenching</th>
      <td class="govuk-table__cell">
          <#if technicalDetailsView.isTrenched>
            The pipeline will not be trenched.
          <#else>
              ${technicalDetailsView.trenchingDescription}
          </#if>
      </td>
    </tr>
    </tbody>
  </table>
  <h4 class="govuk-heading-m">Design codes</h4>
  <table class="govuk-table">
    <tbody class="govuk-table__body">
    <#list technicalDetailsView.pipelineDesignCodeViewList as designCode>
      <tr class="govuk-table__row">
        <th class="govuk-table__header govuk-!-width-one-third">${designCode.title!""}</th>
        <td class="govuk-table__cell">${designCode.description!""}</td>
      </tr>
    </#list>
    </tbody>
  </table>

  <h3 class="govuk-heading-m">Fluid composition</h3>
  <table class="govuk-table">
    <thead class="govuk-table__header">
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Component</th>
      <th class="govuk-table__header">Mol %</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list technicalDetailsView.pipelineFluidChemicalComponentViewList as fluidComposition>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${fluidComposition.componentName!""}</td>
        <td class="govuk-table__cell">
            <#if fluidComposition.isTrace>
              Trace
            <#else>
                ${fluidComposition.molPercentage!""}
            </#if>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <h3 class="govuk-heading-s">Other components affecting design</h3>
  <table class="govuk-table">
    <tbody class="govuk-table__body">
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Wax content</th>
      <td class="govuk-table__cell"> wt%</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Wax appearance temperature</th>
      <td class="govuk-table__cell"> XX °C</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Acid Number (TAN)</th>
      <td class="govuk-table__cell"> &gt;Xmg KOH/g</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Viscosity</th>
      <td class="govuk-table__cell"> CP @ X bar(a)</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Density/Gravity</th>
      <td class="govuk-table__cell"> ${stringUtils.superscriptConverter("Kg/m³")}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Sulphur Content</th>
      <td class="govuk-table__cell"> X wt%</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Pour point</th>
      <td class="govuk-table__cell"> XX °C</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Phases Present</th>
      <td class="govuk-table__cell"> Oil/Condensate/Gas/Water</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Solid Content</th>
      <td class="govuk-table__cell"> X wt%</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Mercury</th>
      <td class="govuk-table__cell"> &gt;${stringUtils.superscriptConverter("Xμg/m³")}</td>
    </tr>

    </tbody>
  </table>

  <h3 class="govuk-heading-m">Design operating limits</h3>
  <table class="govuk-table">
    <thead class="govuk-table__header">
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third"></th>
      <th class="govuk-table__header govuk-!-width-one-third">Operating conditions</th>
      <th class="govuk-table__header govuk-!-width-one-third">Designs conditions</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Temperature (min/max)</th>
      <td class="govuk-table__cell">
          ${technicalDetailsView.operatingTemperatureMin!""}
        / ${technicalDetailsView.operatingTemperatureMax!""}
      </td>
      <td class="govuk-table__cell">
          ${technicalDetailsView.designTemperatureMin!""}
        / ${technicalDetailsView.designTemperatureMax!""}
      </td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Pressure (internal/external)</th>
      <td class="govuk-table__cell">
          ${technicalDetailsView.operatingPressureInternal!""}
        / ${technicalDetailsView.operatingPressureExternal!""}
      </td>
      <td class="govuk-table__cell">${technicalDetailsView.designPressureInternal!""}
        / ${technicalDetailsView.designPressureExternal!""}
      </td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">Flowrate (min/max)</th>
      <td class="govuk-table__cell">
          ${technicalDetailsView.operatingFlowRateMin!""}
        / ${technicalDetailsView.operatingFlowRateMax!""}
      </td>
      <td class="govuk-table__cell">
          ${technicalDetailsView.designFlowRateMax!""}
        / ${technicalDetailsView.designFlowRateMax!""}
      </td>
    </tr>

    <tr class="govuk-table__row">
      <th class="govuk-table__header govuk-!-width-one-third">U-value (W/m<sub>2</sub>K)</th>
      <td class="govuk-table__cell">${technicalDetailsView.operatingUValue!""}</td>
      <td class="govuk-table__cell">${technicalDetailsView.designUValue!""}</td>
    </tr>
    </tbody>
  </table>
</#macro>