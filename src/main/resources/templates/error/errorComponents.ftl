<#macro errorReference ref>
  <#if ref?has_content>
    <p class="govuk-body">
      Error reference: <span class="govuk-!-font-weight-bold">${ref}</span>
    </p>
  </#if>
</#macro>