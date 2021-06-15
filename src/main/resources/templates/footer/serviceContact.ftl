<#include '../layout.ftl'>

<#macro serviceContact serviceContact includeHeader=true>
  <#if includeHeader>
    <h2 class="govuk-heading-m">${serviceContact.displayName}</h2>
    <#if serviceContact.description?has_content>
      <span class="govuk-hint">${serviceContact.description}</span>
    </#if>
  </#if>
  <ul class="govuk-list">
    <li>${serviceContact.serviceName}</li>
    <#if serviceContact.phoneNumber?has_content>
      <li>Telephone: ${serviceContact.phoneNumber}</li>
    </#if>
    <#if serviceContact.emailAddress?has_content>
      <li>Email: <@fdsAction.link linkText=serviceContact.emailAddress linkUrl="mailto:${serviceContact.emailAddress}"/></li>
    </#if>
    <#if serviceContact.guidanceUrl?has_content>
      <li>Online guidance: <@fdsAction.link linkText=serviceContact.guidanceUrl linkUrl=serviceContact.guidanceUrl openInNewTab=true /></li>
    </#if>
  </ul>
</#macro>