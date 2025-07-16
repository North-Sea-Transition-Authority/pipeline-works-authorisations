<#import '/spring.ftl' as spring>
<#import 'fds/components/header/energyPortalHeader.ftl' as fdsEnergyPortalHeader>

<#function springUrl url>
    <#local springUrl>
        <@spring.url url/>
    </#local>
    <#return springUrl>
</#function>

<#macro header wrapperWidth>
  <@fdsEnergyPortalHeader.energyPortalHeader headerLogo="NSTA" userDisplayName=currentUserView.getFullName() signOutUrl=springUrl("/logout") wrapperWidth=wrapperWidth/>
</#macro>
