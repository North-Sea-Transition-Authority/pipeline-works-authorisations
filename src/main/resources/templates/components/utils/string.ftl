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