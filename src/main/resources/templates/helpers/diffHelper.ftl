<!-- Iterates entries list and checks for a DiffedField on each entry with a matching DiffType -->
<!-- Returns the count of entries with the matching DiffType -->
<#function getDiffUnmatchedFieldCount entries field diffType>
    <#if !(entries?has_content)>
        <#return 0>
    </#if>
    <#assign counter = 0>
    <#list entries as entry>
        <#if !(_isFieldOfType(entry, field, diffType))>
            <#assign counter = counter + 1>
        </#if>
    </#list>
    <#return counter>
</#function>

<#function _isFieldOfType entry field diffType>
    <#return entry[field].diffType == diffType>
</#function>