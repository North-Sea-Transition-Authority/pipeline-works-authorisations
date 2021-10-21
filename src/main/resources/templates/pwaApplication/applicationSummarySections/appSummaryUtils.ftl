


<#macro showYesNoForBool value>
  ${value?then('Yes', 'No')}
</#macro>

<#macro showConfirmedForBool value>
  ${value?then('Confirmed', 'Unconfirmed')}
</#macro>

<#macro showNotProvidedWhenEmpty value="" suffix="">
  <#if value?has_content>
    ${value} ${suffix}
  <#else>
    Not provided
  </#if>
</#macro>

