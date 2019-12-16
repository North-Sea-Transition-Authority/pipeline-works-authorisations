<#import '/spring.ftl' as spring>
<#import 'govuk/header.ftl' as govukHeader>
<#import 'govuk/footer.ftl' as govukFooter>
<#import 'govuk/accordion.ftl' as govukAccordion>
<#import 'govuk/breadcrumbs.ftl' as govukBreadcrumbs>
<#import 'govuk/button.ftl' as govukAction>
<#import 'govuk/checkboxes.ftl' as govukCheckboxes>
<#import 'govuk/dateInput.ftl' as govukDateInput>
<#import 'govuk/details.ftl' as govukDetails>
<#import 'govuk/error.ftl' as govukError>
<#import 'govuk/fieldset.ftl' as govukFieldset>
<#import 'govuk/insetText.ftl' as govukInsetText>
<#import 'govuk/numberInput.ftl' as govukNumberInput>
<#import 'govuk/panel.ftl' as govukPanel>
<#import 'govuk/radio.ftl' as govukRadios>
<#import 'govuk/select.ftl' as govukSelect>
<#import 'govuk/tabs.ftl' as govukTabs>
<#import 'govuk/textInput.ftl' as govukTextInput>
<#import 'govuk/pagination.ftl' as govukPagination>
<#import 'govuk/warning.ftl' as govukWarning>
<#import 'govuk/textArea.ftl' as govukTextArea>
<#import 'widgets/cards.ftl' as govukCard>
<#import 'widgets/fileUpload.ftl' as fileUpload>

<#import 'form.ftl' as form>

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>

<#macro defaultPage
  title
  pageHeading=""
  errorCheck=false
  caption=""
  captionClass="govuk-caption-xl"
  twoThirdsColumn=true
  headingCssClass="govuk-heading-xl"
  breadcrumbsNav=false
  backLink=true
  phaseBanner=false>

  <#--Checks if the heading has content in order to not display an empty <h1>-->
  <#local heading=pageHeading?has_content>

<!DOCTYPE html>
<html lang="en" class="govuk-template ">

<head>
  <meta charset="utf-8" />
  <title><#if errorCheck=true>Error: </#if>${title} - MCMS Self Service</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#0b0c0c" />
  <link rel="shortcut icon" sizes="16x16 32x32 48x48" href="<@spring.url'/assets/govuk-frontend/assets/images/favicon.ico'/>" type="image/x-icon" />
  <link rel="mask-icon" href="<@spring.url'/assets/govuk-frontend/assets/images/govuk-mask-icon.svg'/>" color="#0b0c0c">
  <link rel="apple-touch-icon" sizes="180x180" href="<@spring.url'/assets/govuk-frontend/assets/images/govuk-apple-touch-icon-180x180.png'/>">
  <link rel="apple-touch-icon" sizes="167x167" href="<@spring.url'/assets/govuk-frontend/assets/images/govuk-apple-touch-icon-167x167.png'/>">
  <link rel="apple-touch-icon" sizes="152x152" href="<@spring.url'/assets/govuk-frontend/assets/images/govuk-apple-touch-icon-152x152.png'/>">
  <link rel="apple-touch-icon" href="<@spring.url'/assets/govuk-frontend/assets/images/govuk-apple-touch-icon.png'/>">

  <!--[if !IE 8]><!-->
  <link rel="stylesheet" href="<@spring.url'/assets/static/css/main.css'/>">
  <!--<![endif]-->

  <!--[if IE 8]>
  <link rel="stylesheet" href="<@spring.url'/assets/static/css/main-ie8.css'/>">
  <![endif]-->

  <!--[if lt IE 9]>
  <script src="<@spring.url'/assets/html5shiv/src/html5shiv.js'/>"></script>
  <![endif]-->

  <meta property="og:image" content="<@spring.url'/assets/images/govuk-opengraph-image.png'/>">
</head>

<body class="govuk-template__body ">
  <script>
    document.body.className = ((document.body.className) ? document.body.className + ' js-enabled' : 'js-enabled');
  </script>

  <a href="#main-content" class="govuk-skip-link">Skip to main content</a>

  <@govukHeader.header headerNavClass=false currentUserView=currentUserView/>

  <div class="govuk-width-container">
    <#--Phase Banner-->
    <#if phaseBanner>
      <div class="govuk-phase-banner">
        <p class="govuk-phase-banner__content">
          <strong class="govuk-tag govuk-phase-banner__content__tag ">alpha</strong>
          <span class="govuk-phase-banner__text">This is a new service – your <a class="govuk-link" href="#">feedback</a> will help us to improve it.</span>
        </p>
      </div>
    </#if>

    <#if breadcrumbList?has_content>
      <@govukBreadcrumbs.breadcrumbs crumbsList=breadcrumbList currentPage=currentPage/>
    </#if>

    <#if backLink>
      <a href="javascript:window.history.go(-1)" class="govuk-back-link">Back</a>
    </#if>

    <main class="govuk-main-wrapper " id="main-content" role="main">
      <#if twoThirdsColumn>
        <div class="govuk-grid-row">
          <div class="govuk-grid-column-two-thirds">
            <#if caption?has_content>
              <span class="${captionClass}">${caption}</span>
            </#if>
            <#--GOVUK heading class names https://design-system.service.gov.uk/styles/typography/-->
            <#if heading>
              <h1 class="${headingCssClass}">${pageHeading}</h1>
            </#if>

            <#if errorList?has_content>
              <@govukError.errorSummary errorItems=errorList/>
            </#if>

            <#nested>
          </div>

        </div>
        <#else>
          <#if caption?has_content>
            <span class="${captionClass}">${caption}</span>
          </#if>
          <#if heading>
            <h1 class="${headingCssClass}">${pageHeading}</h1>
          </#if>
          <#if errorList?has_content>
            <@govukError.errorSummary errorItems=errorList/>
          </#if>
        <#nested>
      </#if>
    </main>
  </div>

  <@govukFooter.footer/>

  <script src="<@spring.url'/assets/govuk-frontend/all.js'/>"></script>
  <script>
    window.GOVUKFrontend.initAll()
  </script>
  <script src="<@spring.url'/assets/govuk-frontend/common.js'/>"></script>
  <script src="<@spring.url'/assets/govuk-frontend/vendor/polyfills/Element/prototype/classList.js'/>"></script>
</body>

</html>
</#macro>