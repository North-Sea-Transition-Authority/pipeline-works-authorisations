<#-- @ftlvariable name="showWatermark" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentRef" type="String" -->
<#-- @ftlvariable name="sectionHtml" type="String" -->

<html>
<head>
  <style>
    @page {
      size: A4;
      margin: 50px;

      @bottom-right {
        content: counter(page);
        font-size: 14px;
        font-family: "Arial-MT";
      }

      @top-left {
        /*content: element(myheader);*/
        content: element(watermark-ref);
      }
    }

    @font-face {
      font-family: "Arial-MT";
      src: url('document-assets/ArialMT.ttf');
    }

    @font-face {
      font-family: "Arial-MT";
      src: url('document-assets/ArialMT-Bold.ttf');
      font-weight: bold;
    }

    html {
      font-family: "Arial-MT";
      font-size: 16px;
    }

    img {
      width: 30%;
      height: auto;
    }

    watermark {
      position: running(watermark-ref);
      z-index: -999;
      color: #b3b3ff;
      font-size: 100px;
      padding-top: 115mm;
      text-align: center;
      font-weight: bold;
      line-height: 90px;
    }

    #myheader {
      position: running(myheader);
    }

    #image {
      background-image: url('document-assets/oga-logo.png');
      height: 30px;
    }

  </style>
</head>
<body>

<#--  <div id="myheader">-->

<#--    <div id="image"></div>-->

<#--    <div id="header">-->
<#--      <h1>${consentRef!"Pipeline Works Authorisations document"}</h1>-->
<#--    </div>-->

<#--  </div>-->

  <img src="classpath:///document-assets/oga-logo.png"/>
  <h1>${consentRef!"Pipeline Works Authorisations document"}</h1>

  <#if showWatermark>
    <watermark>TEST DOCUMENT</watermark>
  </#if>

  ${sectionHtml?no_esc}

</body>
</html>