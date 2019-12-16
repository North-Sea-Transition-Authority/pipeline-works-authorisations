<#--GOVUK Warning Text-->
<#--https://design-system.service.gov.uk/components/warning-text/-->
<#macro warning>
  <div class="govuk-warning-text">
    <span class="govuk-warning-text__icon" aria-hidden="true">!</span>
    <strong class="govuk-warning-text__text">
      <span class="govuk-warning-text__assistive">Warning</span>
      <#nested>
    </strong>
  </div>
</#macro>