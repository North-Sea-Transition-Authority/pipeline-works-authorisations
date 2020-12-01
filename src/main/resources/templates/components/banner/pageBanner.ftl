<#include '../../layout.ftl'>

<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.model.view.banner.PageBannerView" -->

<#macro banner view showBannerLinks=false>
  <@fdsContactPanel.contactPanel headingText=view.header!"" contentHeadingCaption=view.headerCaption!"" contentHeadingText=view.bodyHeader!"" >
      <#if showBannerLinks && view.bannerLink?has_content >
          <#local linkText = view.bannerLink.text/>
          <#local linkUrl = springUrl(view.bannerLink.url)/>
          <@fdsAction.link linkText=linkText linkClass="govuk-button govuk-button--negative" linkUrl=linkUrl role=true/>
      </#if>
  </@fdsContactPanel.contactPanel>
</#macro>