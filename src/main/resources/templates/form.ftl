<#macro govukForm actionUrl>
  <form action="${actionUrl}" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <#nested>
  </form>
</#macro>

<#macro govukFormRelative style="" class="">
  <form method="post" <#if style?has_content>style="${style}"</#if> <#if class?has_content>class="${class}"</#if>>
    <#if _csrf?has_content>
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </#if>
    <#nested>
  </form>
</#macro>