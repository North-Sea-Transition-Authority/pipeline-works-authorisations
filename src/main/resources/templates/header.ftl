<#import '/spring.ftl' as spring>
<#import 'fds/components/header/energyPortalHeader.ftl' as fdsEnergyPortalHeader>

<#function springUrl url>
    <#local springUrl>
        <@spring.url url/>
    </#local>
    <#return springUrl>
</#function>

<#macro header wrapperWidth>
  <#assign userFullName>
    <#if currentUserView?has_content && currentUserView.isAuthenticated()>
      ${currentUserView.getFullName()}
    </#if>
  </#assign>
  <@fdsEnergyPortalHeader.energyPortalHeader headerLogo="NSTA" userDisplayName=userFullName signOutUrl=springUrl("/logout") wrapperWidth=wrapperWidth/>
</#macro>
