<#--GOVUK Details-->
<#--https://design-system.service.gov.uk/components/details/-->
<#macro details summaryText summaryTitle>
  <details class="govuk-details">
    <summary class="govuk-details__summary">
      <span class="govuk-details__summary-text">
        ${summaryTitle}
      </span>
    </summary>
    <div class="govuk-details__text">
      <#noautoesc>
        ${summaryText}
      </#noautoesc>
    </div>
  </details>
</#macro>

<#macro summaryDetails summaryTitle>
  <details class="govuk-details">
    <summary class="govuk-details__summary">
      <span class="govuk-details__summary-text">
        ${summaryTitle}
      </span>
    </summary>
    <div class="govuk-details__text">
      <#nested>
    </div>
  </details>
</#macro>