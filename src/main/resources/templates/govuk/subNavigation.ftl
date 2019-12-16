<#import '/spring.ftl' as spring>
<#--GDS Design System Sub Navigation-->

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>

<#macro subnav navigation currentEndPoint>
  <nav class="app-navigation govuk-clearfix">
    <ul class="app-navigation__list govuk-width-container">
      <#list navigation as item>
        <li <#if springUrl(item.url)?markup_string?starts_with(currentEndPoint)>class="app-navigation--current-page"</#if>>
          <a class="govuk-link" href="${springUrl(item.url)}" data-topnav="${item.name}">${item.name}</a>
        </li>
      </#list>
    </ul>
  </nav>

  <nav id="app-mobile-nav" class="app-mobile-nav js-app-mobile-nav" role="navigation">
    <ul class="app-mobile-nav__list">
      <#list navigation as item>
      <li>
        <a class="govuk-link js-mobile-nav-subnav-toggler{% if item.url in path %} app-mobile-nav__subnav-toggler--active{% endif %}" href="${springUrl(item.url)}">${item.name}</a>
      </li>
      </#list>
    </ul>
  </nav>
</#macro>