<#--Layout-->
<#include 'fds/objects/layouts/generic.ftl'>
<#import 'fds/objects/grid/grid.ftl' as grid>
<#import 'header.ftl' as pipelinesHeader>

<#macro defaultPagePane
  htmlTitle
  wrapperClasses=""
  wrapperWidth=false
  topNavigation=false
  phaseBanner=true
  phaseBannerLink="#"
  headerLogo="FIVIUM"
  errorCheck=false
  noIndex=false>

    <@genericLayout htmlTitle=htmlTitle htmlAppTitle="OGA Pipelines" errorCheck=errorCheck noIndex=noIndex>
      <div class="fds-pane fds-pane--enabled" id="top">
          <#--Header goes below me-->
          <@pipelinesHeader.header logoText="OGA" logoProductText="" headerNav=true serviceName="Pipeline Works Authorisations" topNavigation=topNavigation wrapperWidth=wrapperWidth headerIcon=headerIcon/>

          <#--Phase banner goes below me-->
          <#if phaseBanner>
            <div class="govuk-phase-banner__wrapper">
              <div class="govuk-phase-banner govuk-phase-banner--no-border<#if wrapperWidth> govuk-width-container-wide<#else> govuk-width-container</#if>">
                <p class="govuk-phase-banner__content">
                  <strong class="govuk-tag govuk-phase-banner__content__tag ">alpha</strong>
                  <span class="govuk-phase-banner__text">This is a new service – your <a class="govuk-link" href="${phaseBannerLink}">feedback</a> will help us to improve it.</span>
                </p>
              </div>
            </div>
          </#if>

          <#--Top navigation goes below me-->
          <#if topNavigation>
              <@fdsNavigation.navigation navigationItems=navigationItems currentEndPoint=currentEndPoint wrapperWidth=wrapperWidth/>
          </#if>

        <div
          class="fds-pane__body ${wrapperClasses}<#if wrapperWidth> govuk-width-container-wide<#else> govuk-width-container</#if>">
            <#nested>

            <#--Back to top goes below me-->
            <@fdsBackToTop.backToTop/>
        </div>

          <#--Footer goes below me-->
          <@fdsFooter.footer wrapperWidth=wrapperWidth/>
      </div>
    </@genericLayout>
</#macro>

<#macro defaultPagePaneContent
mainClasses=""
captionClass="govuk-caption-xl"
caption=""
pageHeadingClass="govuk-heading-xl"
pageHeading="">

  <div class="fds-pane__content">
    <main id="main-content" class="fds-content ${mainClasses}" role="main">
      <div class="fds-content__header">
          <@defaultHeading caption=caption captionClass=captionClass pageHeading=pageHeading pageHeadingClass=pageHeadingClass/>
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