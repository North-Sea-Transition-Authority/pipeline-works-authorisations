<#macro hiddenInput path>
  <@spring.bind path/>
  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local name=spring.status.expression>
  <#local value=spring.stringStatusValue>

  <input type="hidden" id="${id}" name="${name}" value="${value}">
</#macro>