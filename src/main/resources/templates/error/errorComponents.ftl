<#macro techSupportInfo>
  <p class="govuk-body">
    For technical support call 0191 376 2660. This service is available from 8.30am to 5pm from Monday to Friday, excluding public holidays.
  </p>
</#macro>

<#macro errorReference ref>
  <#if ref?has_content>
    <p class="govuk-body">
      Error reference: <span class="govuk-!-font-weight-bold">${ref}</span>
    </p>
  </#if>
</#macro>