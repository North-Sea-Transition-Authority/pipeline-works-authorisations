<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="combinedSummaryHtml" type="java.lang.String" -->
<#-- @ftlvariable name="sidebarSectionLinks" type="java.util.List<uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink>" -->
<#-- @ftlvariable name="sidebarLink" type="uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink" -->




<#assign pageHeading="Review and Submit Application XXX/YYY/ZZZ"/>

<#macro renderSidebarLink sidebarLink>
    <#local linkUrl = sidebarLink.isAnchorLink?then(sidebarLink.link, springUrl(sidebarLink.link) )>

    <@fdsSubNavigation.subNavigationSectionItem
    linkName=sidebarLink.displayText
    currentItemHref="#top"
    linkAction=linkUrl
    />

</#macro>
<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>
            <@fdsSubNavigation.subNavigationSection themeHeading="Check your answers for all questions in the application">

                <#list sidebarSectionLinks as sidebarLink>
                  <@renderSidebarLink sidebarLink=sidebarLink/>

                </#list>

            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent pageHeading=pageHeading>
      <!-- This should be supported by the fds component. but it isnt. spent enough time faffing around with LayoutPane.ftl
       to add the same div as in layout.ftl but the css goes nuts and everything breaks.
         Located here to make navigating back from this page easy for basic testing.
         -->
        <@fdsBreadcrumbs.breadcrumbs crumbsList=breadcrumbMap currentPage=currentPage/>

        ${combinedSummaryHtml?no_esc}

        <@fdsForm.htmlForm>
            <@fdsAction.button buttonText="Submit" buttonValue="submit" />
        </@fdsForm.htmlForm>
    </@defaultPagePaneContent>




</@defaultPagePane>