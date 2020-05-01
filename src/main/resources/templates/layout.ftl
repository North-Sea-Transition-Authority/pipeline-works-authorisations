 <#import '/spring.ftl' as spring/>

<#--Layout-->
<#include 'fds/objects/layouts/generic.ftl'>
<#import 'fds/objects/grid/grid.ftl' as grid>
<#import 'header.ftl' as pipelinesHeader>
<#import 'components/fileUpload/fileUpload.ftl' as fileUpload>
<#import 'components/completedTag/completedTag.ftl' as completedTag>
<#import 'components/coordinates/locationInput.ftl' as locationInput>
<#import 'components/taskList/pwaTaskListItem.ftl' as pwaTaskListItem>

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
  twoThirdsColumn=true
  oneQuarterColumn=false
  backLink=false
  backLinkUrl=""
  backLinkText="Back"
  breadcrumbs=false
  phaseBanner=false
  phaseBannerLink="#"
  topNavigation=false
  wrapperWidth=false
  masthead=false
  headerIcon=true>

  <@genericLayout htmlTitle=htmlTitle htmlAppTitle="OGA Pipelines">

    <#--Header-->
    <@pipelinesHeader.header logoText="OGA" logoProductText="" headerNav=true serviceName="Pipeline Works Authorisations" topNavigation=topNavigation wrapperWidth=wrapperWidth headerIcon=headerIcon/>

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
      <@fdsBreadcrumbs.breadcrumbs crumbsList=breadcrumbMap currentPage=currentPage/>
    </#if>

    <#--Back link-->
    <#if backLink && !breadcrumbs>
      <@fdsBackLink.backLink backLinkUrl=backLinkUrl backLinkText=backLinkText/>
    </#if>

    <main class="${mainClasses}" id="main-content" role="main">
      <#--Grid-->
      <#if fullWidthColumn>
        <@grid.gridRow>
          <@grid.fullColumn>
            <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
            <#nested>
          </@grid.fullColumn>
        </@grid.gridRow>
      <#elseif oneHalfColumn>
        <@grid.gridRow>
          <@grid.oneHalfColumn>
            <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
            <#nested>
          </@grid.oneHalfColumn>
        </@grid.gridRow>
      <#elseif oneThirdColumn>
        <@grid.gridRow>
          <@grid.oneThirdColumn>
            <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
            <#nested>
          </@grid.oneThirdColumn>
        </@grid.gridRow>
      <#elseif twoThirdsColumn>
        <@grid.gridRow><@grid.twoThirdsColumn>
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
          <#nested>
        </@grid.twoThirdsColumn>
        </@grid.gridRow>
      <#elseif oneQuarterColumn>
        <@grid.gridRow>
          <@grid.oneQuarterColumn>
            <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
            <#nested>
          </@grid.oneQuarterColumn>
        </@grid.gridRow>
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

  <#--Custom scripts go here-->
    <script src="<@spring.url '/assets/static/js/vendor/jquery/jquery.iframe-transport.min.js'/>"></script>
    <!-- TODO remove jquery UI dependency, investigate impact on modals -->
    <script src="<@spring.url '/assets/static/js/vendor/jquery/jquery-ui.min.js'/>"></script>
    <script src="<@spring.url '/assets/static/js/vendor/jquery/jquery.fileupload.min.js'/>"></script>

  </@genericLayout>
</#macro>