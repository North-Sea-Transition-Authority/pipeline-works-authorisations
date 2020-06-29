<#macro leftPad value leftPadAmount leftPadCharacter>
    <#assign split = value?split(".")/>
    <#-- Return a value with the left side of the decimal place padded -->
    ${([split[0]?left_pad(leftPadAmount, leftPadCharacter)] + split[1..])?join(".")}
</#macro>