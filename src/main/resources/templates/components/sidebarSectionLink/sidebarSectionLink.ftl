<#include '../../layoutPane.ftl'>

<#-- @ftlvariable name="sidebarLink" type="uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink" -->

<#macro renderSidebarLink sidebarLink>
    <#local linkUrl = sidebarLink.isAnchorLink?then(sidebarLink.link, springUrl(sidebarLink.link))>

    <@fdsSubNavigation.subNavigationSectionItem
    linkName=sidebarLink.displayText
    currentItemHref="#top"
    linkAction=linkUrl />

</#macro>