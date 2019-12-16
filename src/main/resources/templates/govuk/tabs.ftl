<#import '/spring.ftl' as spring>

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>

<#--GOVUK Tabs Widget-->
<#--https://design-system.service.gov.uk/components/tabs/-->
<#macro tabs tabsHeading>
  <div class="govuk-tabs" data-module="tabs">
    <h2 class="govuk-tabs__title">
      ${tabsHeading}
    </h2>
    <#nested>
  </div>
</#macro>

<#macro tabList>
  <ul class="govuk-tabs__list">
    <#nested>
  </ul>
</#macro>

<#macro tab tabLabel tabValue="" tabAnchor="" tabUrl="" currentTab="" tabSelected=false>
  <#if currentTab?has_content>
    <#assign selected = currentTab == tabValue>
  <#elseif tabSelected=true>
    <#assign selected = true>
  <#else>
    <#assign selected = false>
  </#if>
  <#if !tabUrl?has_content>
    <#assign tabLink = tabAnchor>
    <#assign anchor = "#">
  <#else>
    <#assign tabLink = springUrl(tabUrl)>
    <#assign anchor = "">
  </#if>

  <li class="govuk-tabs__list-item">
    <a class="govuk-tabs__tab <#if selected=true>govuk-tabs__tab--selected</#if>" href="${anchor}${tabLink}">
      ${tabLabel}
    </a>
  </li>
</#macro>

<#macro tabContent tabLabel tabAnchor tabValue="" currentTab="" tabSelected=false>
  <#if currentTab?has_content>
    <#assign selected = currentTab == tabValue>
  <#elseif tabSelected=true>
    <#assign selected = true>
  <#else>
    <#assign selected = false>
  </#if>
  <section class="govuk-tabs__panel <#if selected=false>govuk-tabs__panel--hidden</#if>" id="${tabAnchor}">
    <#nested>
  </section>
</#macro>