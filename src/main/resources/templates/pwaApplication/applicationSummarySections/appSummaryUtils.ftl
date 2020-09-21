


<#macro showYesNoForBool value>
  ${value?then('Yes', 'No')}
</#macro>

<#macro showConfirmedForBool value>
  ${value?then('Confirmed', 'Unconfirmed')}
</#macro>

<#macro showNotProvidedWhenEmpty value>
  <#if value?has_content>
    ${value}
  <#else>
    Not provided
  </#if>
</#macro>

