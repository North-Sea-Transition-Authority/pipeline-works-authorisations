<#-- @ftlvariable name="showWatermark" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentRef" type="String" -->
<#-- @ftlvariable name="sectionHtml" type="String" -->
<#-- @ftlvariable name="issueDate" type="String" -->

<html>
<head>
  <style>
    @page {
      size: A4;
      margin: 135px 50px 50px;

      @bottom-right {
        content: counter(page);
        font-size: 14px;
        font-family: "Arial-MT";
      }

      @top-left-corner {
        content: element(header-content);
      }

      @bottom-center {
        content: element(issuer-content);
      }

      @top-center {
        content: element(watermark-ref);
      }

    }

    @page landscapePage {
      size: A4 landscape;
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

    #header-content {
      position: running(header-content);
      padding-top: 50px;
      height: auto;
      padding-left: 50px;
    }

    .issuer-content {
      position: running(issuer-content);
      padding-bottom: 10px;
      padding-left: 20px;
      padding-right: 20px;
    }

    .issuer-content p {
      text-align: center;
      margin: 0;
      padding: 0;
      width: 100%;
    }

    .logo-image {
      width: 30%;
      max-height: 30px;
    }

    .image {
      width:auto;
      max-height: 800px;
    }

    .header-heading {
      margin-top: 5px;
    }

    .header-issue-info {
      font-size: 14px;
    }

    .nsta-disclaimer {
      font-size: 8px;
    }

    table {
      /* Repeats the header and footer on each page that the table is on. */
      -fs-table-paginate: paginate;
    }

    .sectionTable, #depositsTableSection {
      width: 1000px;
    }

    .sectionTable, .sectionTable th, .sectionTable td {
      border: 1px solid black;
      border-collapse: collapse;
    }

    .sectionTable td {
      padding-left: 5px;
    }

    .coordinateTableCell {
      width: 140px;
    }

    @media print {
      #depositsTableSection, #tableAsSection, .tableAPage, .tableADrawing  {
        page-break-before: always;
      }
    }

    .sectionTable tr {
      page-break-inside:avoid;
      page-break-after:auto
    }

    .sectionTable, #depositsTableSection, .tableAPage {
      page: landscapePage;
    }

    #depositsTableSection .materialTypeAndSize {
      word-wrap: normal
    }

    .drawingNumberList {
      list-style-type:none;
      padding-left: 0;
    }

    ol {
      padding-left: 20px;
    }

    .govuk-list--lower-alpha {
      list-style-type: lower-alpha;
    }

    .govuk-list--lower-roman {
      list-style-type: lower-roman;
    }

    .tableA {
      page-break-inside:auto;
      font-size: 12px;
    }

    .tableA th {
      text-align: center;
    }

    .tableA .headerRow {
      vertical-align: top;
    }

    #huooSection {
      text-align: center;
    }

    #huooSection .huooPipelines, #huooSection .roleTypeTxt {
      font-weight: bold;
      text-decoration: underline;
    }

    #huooSection .orgRoleAndPipelines {
      margin-top: 40px;
    }

    .huooTreatyDescription {
      width: 650px;
    }

    .huooOrgDescription {
      page-break-inside:avoid;
    }

    .pwa-clause-list__list-item {
      margin-top: 15px;
    }

    .digital-signature {
      list-style: none;
      padding: 0;
      margin: 0;
      min-height: 55px;
      page-break-inside: avoid;
    }

    .multi-line-text {
      white-space: pre-line;
      margin-bottom: 20px;
      display: block;
    }

    .pwa-mail-merge__preview--automatic {
      color: #00703c;
      font-weight: bold;
    }

    .pwa-mail-merge__preview--manual {
      color: #d4351c;
      font-weight: bold;
      text-decoration: underline;
    }

    .pwa-clause-section-header {
      text-align: center;
    }

    .govuk-visually-hidden {
      display: none;
    }

    .pwa-intro-paragraph, .pwa-intro-paragraph > * {
      text-align: justify;
      padding-top: 25px;
    }

    .full-width {
      width: 1000px;
    }

    .page-break {
      page-break-after: always;
    }

  </style>
</head>
<body>

  <div id="header-content">

    <img class="logo-image" src="classpath:///document-assets/nsta-logo.png"/>

    <h1 class="header-heading">${consentRef!"Pipeline Works Authorisations document"}</h1>

  </div>

  <div class="issuer-content">
    <p class="header-issue-info">Authorised and Issued by the Oil and Gas Authority ${issueDate}</p>
    <p class="nsta-disclaimer">North Sea Transition Authority is a business name of the Oil and Gas Authority. Oil and Gas Authority is a limited company registered in England and Wales with registered number 09666504 and VAT registered number 249433979. Our registered office is at Sanctuary Buildings, 20 Great Smith Street, London, SW1P 3BT</p>
  </div>

  <#if showWatermark>
    <watermark>PREVIEW DOCUMENT</watermark>
  </#if>

  ${sectionHtml?no_esc}

</body>
</html>