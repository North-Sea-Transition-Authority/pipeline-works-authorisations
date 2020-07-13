<#macro pluralise count word>
    <#if count == 1>
        ${count} ${word}
    <#else>
        ${count} ${word}s
    </#if>
</#macro>

<#macro pluraliseWord count word>
    <#if count == 1>
        ${word}
    <#else>
        ${word}s
    </#if>
</#macro>

<#function unicodeNumberConverter value>
    <#local character = value[0..0]/>
    <#if character == "₁" || character == "¹">
        <#assign returnValue = "1"/>
    <#elseif character == "₂" || character == "²">
        <#assign returnValue = "2"/>
    <#elseif character == "₃" || character == "³">
        <#assign returnValue = "3"/>
    <#elseif character == "₄" || character == "⁴">
        <#assign returnValue = "4"/>
    <#elseif character == "₅" || character == "⁵">
        <#assign returnValue = "5"/>
    <#elseif character == "₆" || character == "⁶">
        <#assign returnValue = "6"/>
    <#elseif character == "₇" || character == "⁷">
        <#assign returnValue = "7"/>
    <#elseif character == "₈" || character == "⁸">
        <#assign returnValue = "8"/>
    <#elseif character == "₉" || character == "⁹">
        <#assign returnValue = "9"/>
    <#elseif character == "₀" || character == "⁰">
        <#assign returnValue = "0"/>
    <#else>
        <#assign returnValue = character/>
    </#if>
    <#if "${value}"?length gt 1>
        <#return "${returnValue}${converter(value[1..])}">
    <#else>
        <#return "${returnValue}">
    </#if>
</#function>

<#function subscriptConverter text>
    <#assign regexMatch = text?matches(r"(₁|₂|₃|₄|₅|₆|₇|₈|₉|₀)+")>
    <#assign newValue = text>
    <#if regexMatch?has_content>
        <#list regexMatch as group>
            <#assign newValue = newValue?replace(group, "<sub>" + unicodeNumberConverter(group) + "</sub>", "f")>
        </#list>
    </#if>
    <#return newValue?no_esc/>
</#function>

<#function superscriptConverter text>
    <#assign regexMatch = text?matches(r"(¹|²|³|⁴|⁵|⁶|⁷|⁸|⁹|⁰)+")>
    <#assign newValue = text>
    <#if regexMatch?has_content>
        <#list regexMatch as group>
            <#assign newValue = newValue?replace(group, "<sup>" + unicodeNumberConverter(group) + "</sup>", "f")>
        </#list>
    </#if>
    <#return newValue?no_esc/>
</#function>

<#macro superscriptConverterMacro text>
    ${superscriptConverter(text)}
</#macro>

<#macro subscriptConverterMacro text>
    ${subscriptConverter(text)}
</#macro>


