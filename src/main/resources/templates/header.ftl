<#import 'fds/components/header/header.ftl' as fdsHeader>

<#-- Header Component https://design-system.service.gov.uk/components/header/ -->
<#macro header
homePageUrl="/"
serviceUrl="/"
headerClasses=""
containerClasses=""
topNavigation=false
logoText="FIV"
logoProductText="Design System"
headerNav=false
<#--'serviceName' is shown if 'headerNav' is true-->
serviceName="Fivium Design System"
wrapperWidth=false
headerIcon=true>

  <@fdsHeader.header logoText=logoText logoProductText="" headerNav=true serviceName=serviceName topNavigation=topNavigation wrapperWidth=wrapperWidth headerLogo="">
      <@fdsHeader.headerNavigation>
          <#if currentUserView?has_content && currentUserView.isAuthenticated()>
              <@fdsHeader.headerNavigationItem itemText=currentUserView.getFullName()/>
              <@fdsHeader.headerNavigationItem itemText="Sign out" itemUrl=foxLogoutUrl/>
          </#if>
      </@fdsHeader.headerNavigation>
  </@fdsHeader.header>
</#macro>