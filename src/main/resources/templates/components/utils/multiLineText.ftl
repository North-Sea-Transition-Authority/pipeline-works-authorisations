<#macro multiLineText blockClass="">
  <#local nested><#nested/></#local>
  <#if blockClass?has_content>
    <span class="${blockClass}__preserve-whitespace">${nested}</span>
    <#else>
      ${nested}
  </#if>
</#macro>