<#--Layout-->
<#include 'pwaLayoutImports.ftl'>

<#macro defaultPagePane
htmlTitle
wrapperClasses=""
wrapperWidth=false
topNavigation=false
errorCheck=false
noIndex=false
backToTopLink=true>

    <@genericLayout htmlTitle=htmlTitle htmlAppTitle="NSTA Pipelines" errorCheck=errorCheck noIndex=noIndex>
      <div class="fds-pane fds-pane--enabled" id="top">
          <#--Header goes below me-->
          <@pipelinesHeader.header wrapperWidth=wrapperWidth/>

          <#--Top navigation goes below me-->
          <#if topNavigation>
              <@fdsNavigation.navigation serviceName=service.getServiceAcronym() serviceUrl=springUrl(service.getServiceUrl()) navigationItems=navigationItems currentEndPoint=currentEndPoint wrapperWidth=false/>
          </#if>

        <div
          class="fds-pane__body ${wrapperClasses}<#if wrapperWidth> govuk-width-container-wide<#else> govuk-width-container</#if>">
            <#nested>

            <#--Back to top goes below me-->
            <#if backToTopLink>
              <@fdsBackToTop.backToTop/>
            </#if>
        </div>

        <#--Footer goes below me-->
        <@fdsNstaFooter.nstaFooter wrapperWidth=wrapperWidth/>

        <@pwaCustomScripts />

      </div>
    </@genericLayout>
</#macro>

<#macro defaultPagePaneContent
mainClasses=""
captionClass="govuk-caption-xl"
caption=""
pageHeadingClass="govuk-heading-xl"
pageHeading=""
backLink=false
backLinkUrl=""
backLinkText="Back"
breadcrumbs=false
errorItems=[]
singleErrorMessage="">

  <div class="fds-pane__content">
    <main id="main-content" class="fds-content ${mainClasses}" role="main">
        <#--Breadcrumbs-->
        <#if breadcrumbs && !backLink>
            <@fdsBreadcrumbs.breadcrumbs crumbsList=breadcrumbMap currentPage=currentPage/>
        </#if>

        <#--Back link-->
        <#if backLink && !breadcrumbs>
            <@fdsBackLink.backLink backLinkUrl=backLinkUrl backLinkText=backLinkText/>
        </#if>

        <@pwaFlash.flashContent flashTitle=flashTitle flashMessage=flashMessage flashClass=flashClass!"" flashBulletList=flashBulletList![]/>

        <#if notificationBannerView??>
            <@notificationBanner.infoNotificationBanner notificationBannerView/>
        </#if>

      <div class="fds-content__header">
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems singleErrorMessage=singleErrorMessage/>
      </div>
        <#nested>
    </main>
  </div>
</#macro>

<#macro defaultPagePaneSubNav smallSubnav=false>
  <div class="fds-pane__subnav <#if smallSubnav>fds-pane__subnav--small</#if>">
      <#nested>
  </div>
</#macro>
