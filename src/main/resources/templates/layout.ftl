<#import '/spring.ftl' as spring/>

<#--Layout-->
<#include 'fds/objects/layouts/generic.ftl'>
<#import 'fds/objects/grid/grid.ftl' as grid>
<#import 'header.ftl' as pipelinesHeader>

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>

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
  twoThirdsColumn=false
  oneQuarterColumn=false
  backLink=false
  backLinkUrl=""
  backLinkText="Back"
  breadcrumbs=false
  phaseBanner=true
  phaseBannerLink="#"
  topNavigation=false
  wrapperWidth=false
  masthead=false
  headerIcon=true>

  <@genericLayout htmlTitle=htmlTitle htmlAppTitle="OGA Pipelines">

    <#--Header-->
    <@pipelinesHeader.header logoText="OGA" logoProductText="" headerNav=true serviceName="Pipelines"  topNavigation=topNavigation wrapperWidth=wrapperWidth headerIcon=headerIcon/>

    <#--Phase banner-->
    <#if phaseBanner>
      <div class="govuk-phase-banner <#if wrapperWidth> govuk-width-container-wide<#else> govuk-width-container </#if>">
        <p class="govuk-phase-banner__content">
          <strong class="govuk-tag govuk-phase-banner__content__tag ">alpha</strong>
          <span class="govuk-phase-banner__text">This is a new service – your <a class="govuk-link" href="${phaseBannerLink}">feedback</a> will help us to improve it.</span>
        </p>
      </div>
    </#if>

    <#--Navigation-->
    <#if topNavigation>
      <@fdsNavigation.navigation navigationItems=navigationItems currentEndPoint=currentEndPoint wrapperWidth=wrapperWidth />
    </#if>

    <#if !masthead>
      <div class="<#if wrapperWidth>govuk-width-container-wide<#else> govuk-width-container </#if>${wrapperClasses}">
    </#if>

    <#--Breadcrumbs-->
    <#if breadcrumbs && !backLink>
      <@fdsBreadcrumbs.breadcrumbs crumbsList="" currentPage=currentEndPoint/>
    </#if>

    <#--Back link-->
    <#if backLink && !breadcrumbs>
      <@fdsBackLink.backLink backLinkUrl=backLinkUrl backLinkText=backLinkText/>
    </#if>

    <main class="${mainClasses}" id="main-content" role="main">
      <#--Grid-->
      <#if fullWidthColumn>
        <@grid.fullColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.fullColumn>
      <#elseif oneHalfColumn>
        <@grid.oneHalfColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.oneHalfColumn>
      <#elseif oneThirdColumn>
        <@grid.oneThirdColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.oneThirdColumn>
      <#elseif twoThirdsColumn>
        <@grid.twoThirdsColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.twoThirdsColumn>
      <#elseif oneQuarterColumn>
        <@grid.oneQuarterColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.oneQuarterColumn>
      <#else>
        <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
        <#nested>
      </#if>
    </main>

    <#if !masthead>
      </div>
    </#if>

    <#--Footer-->
    <@fdsFooter.footer wrapperWidth=wrapperWidth/>

  </@genericLayout>
</#macro>