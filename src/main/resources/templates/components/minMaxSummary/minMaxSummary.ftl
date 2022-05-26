<#-- Summarising min max values -->

<#-- @ftlvariable name="minValue" type=java.lang.String -->
<#-- @ftlvariable name="maxValue" type=java.lang.String -->
<#-- @ftlvariable name="minPrompt" type=java.lang.String -->
<#-- @ftlvariable name="maxPrompt" type=java.lang.String -->
<#-- @ftlvariable name="unit" type=java.lang.String -->

<#macro minMaxSummary minValue maxValue minPrompt maxPrompt unit>
    <#if minValue?has_content && maxValue?has_content && minValue == maxValue>
        ${minValue} ${unit}
    <#else>
        <#if minValue?has_content>
            ${minPrompt}: ${minValue} ${unit} </br>
        </#if>
        <#if maxValue?has_content>
            ${maxPrompt}: ${maxValue} ${unit}
        </#if>
    </#if>
</#macro>