


<#macro showYesNoForBool value>
  ${value?then('Yes', 'No')}
</#macro>

<#macro showConfirmedForBool value>
  ${value?then('Confirmed', 'Unconfirmed')}
</#macro>

