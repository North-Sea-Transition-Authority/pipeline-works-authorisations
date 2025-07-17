<#include 'layout.ftl'/>

<#-- Header Component https://design-system.service.gov.uk/components/header/ -->
<#macro header
topNavigation=false
logoText="FIV"
logoProductText="Design System"
headerNav=false
<#--'serviceName' is shown if 'headerNav' is true-->
serviceName="Fivium Design System"
wrapperWidth=false>

  <@fdsHeader.header logoText=logoText logoProductText="" headerNav=true serviceName=serviceName topNavigation=topNavigation wrapperWidth=wrapperWidth headerLogo="">
      <@fdsHeader.headerNavigation>
          <#if currentUserView?has_content && currentUserView.isAuthenticated()>
              <@fdsHeader.headerNavigationItem itemText=currentUserView.getFullName()/>
              <@fdsHeader.headerNavigationSignOutButton buttonText="Sign out" formUrl=springUrl("/logout")/>
          </#if>
      </@fdsHeader.headerNavigation>
  </@fdsHeader.header>
</#macro>
