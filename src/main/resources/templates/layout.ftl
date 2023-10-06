<#include 'pwaLayoutImports.ftl'>

<#macro cookieBannerContent>
  <@fdsCookieBanner.analyticsCookieBanner serviceName=service.serviceName cookieSettingsUrl=springUrl(cookiePrefsUrl)/>
</#macro>

<#macro defaultPage
htmlTitle
mainClasses="govuk-main-wrapper"
wrapperClasses=""
pageHeading=""
pageHeadingClass="govuk-heading-xl"
caption=""
captionClass="govuk-caption-xl"
fullWidthColumn=false
oneHalfColumn=false
oneThirdColumn=false
twoThirdsColumn=true
twoThirdsOneThirdColumn=false
oneQuarterColumn=false
twoThirdsOneThirdContent=""
backLink=false
backLinkUrl=""
backLinkText="Back"
breadcrumbs=false
phaseBanner=true
phaseBannerLink=feedbackUrl
topNavigation=false
wrapperWidth=false
masthead=false
errorCheck=false
noIndex=false
headerIcon=true
errorItems=[]>

    <@genericLayout
      htmlTitle=htmlTitle
      htmlAppTitle="${service.customerMnemonic} Pipelines"
      errorCheck=errorCheck
      noIndex=noIndex
      cookieBannerMacro=cookieBannerContent
      disableOnSubmit=true>

    <#--Header-->
    <@pipelinesHeader.header logoText=service.customerMnemonic logoProductText="" headerNav=true serviceName=service.serviceName topNavigation=topNavigation wrapperWidth=wrapperWidth/>

    <#--Phase banner-->
        <#if phaseBanner>
          <div
            class="govuk-phase-banner <#if wrapperWidth> govuk-width-container-wide<#else> govuk-width-container </#if>">
            <p class="govuk-phase-banner__content">
               <strong class="govuk-tag govuk-phase-banner__content__tag ">beta</strong>
               <span class="govuk-phase-banner__text">
                <span>This is a new service – your</span>
                <@fdsAction.link
                    linkText="feedback"
                    linkUrl=springUrl(phaseBannerLink)
                    openInNewTab=false
                    linkClass="govuk-link govuk-link--no-visited-state"
                />
                <span> will help us to improve it.</span>
               </span>
            </p>
          </div>
        </#if>

    <#--Navigation-->
    <#if topNavigation>
        <@fdsNavigation.navigation navigationItems=navigationItems currentEndPoint=currentEndPoint wrapperWidth=wrapperWidth />
    </#if>

    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.appTag />
    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.globalTag />

    <#if !masthead>
      <div class="<#if wrapperWidth>govuk-width-container-wide<#else> govuk-width-container </#if>${wrapperClasses}">
          </#if>

          <#--Breadcrumbs-->
          <#if breadcrumbs && !backLink>
              <@fdsBreadcrumbs.breadcrumbs crumbsList=breadcrumbMap currentPage=currentPage/>
          </#if>

          <#--Back link-->
          <#if backLink && !breadcrumbs>
              <@fdsBackLink.backLink backLinkUrl=backLinkUrl backLinkText=backLinkText/>
          </#if>

          <#assign flash>
            <@pwaFlash.flashContent flashTitle=flashTitle flashMessage=flashMessage flashClass=flashClass!"" flashBulletList=flashBulletList![] />
          </#assign>

        <main class="${mainClasses}" id="main-content" role="main">
            <#--Grid goes below me-->
            <#if fullWidthColumn>
                <@grid.gridRow>
                    <@grid.fullColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.fullColumn>
                </@grid.gridRow>
            <#elseif oneHalfColumn>
                <@grid.gridRow>
                    <@grid.oneHalfColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.oneHalfColumn>
                </@grid.gridRow>
            <#elseif oneThirdColumn>
                <@grid.gridRow>
                    <@grid.oneThirdColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.oneThirdColumn>
                </@grid.gridRow>
            <#elseif twoThirdsColumn>
                <@grid.gridRow>
                    <@grid.twoThirdsColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.twoThirdsColumn>
                </@grid.gridRow>
            <#elseif oneQuarterColumn>
                <@grid.gridRow>
                    <@grid.oneQuarterColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.oneQuarterColumn>
                </@grid.gridRow>
            <#elseif twoThirdsOneThirdColumn>
                <@grid.gridRow>
                    <@grid.twoThirdsColumn>
                        ${flash}
                        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                        <#nested>
                    </@grid.twoThirdsColumn>
                    <@grid.oneThirdColumn>
                        ${twoThirdsOneThirdContent}
                    </@grid.oneThirdColumn>
                </@grid.gridRow>
            <#else>
                ${flash}
                <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass errorItems=errorItems/>
                <#nested>
            </#if>
        </main>

          <#if !masthead>
      </div>
    </#if>

    <#--Footer-->
    <#local footerMetaContent>
      <@fdsFooter.footerMeta footerMetaHiddenHeading="Support links">
        <#list footerItems?sort_by("displayOrder") as footerItem>
          <@fdsFooter.footerMetaLink linkText=footerItem.displayName linkUrl=springUrl(footerItem.url) />
        </#list>
      </@fdsFooter.footerMeta>
    </#local>
    <@fdsNstaFooter.nstaFooter wrapperWidth=wrapperWidth metaLinks=true footerMetaContent=footerMetaContent/>

    <#--Custom scripts go here-->
    <@pwaCustomScripts />

    </@genericLayout>

</#macro>
