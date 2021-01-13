<#-- @ftlvariable name="showWatermark" type="java.lang.Boolean" -->
<#-- @ftlvariable name="consentRef" type="String" -->
<#-- @ftlvariable name="sectionHtml" type="String" -->

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

      @top-left {
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
      padding-left: 55px
    }

    .logo-image {
      width: 30%;
      max-height: 30px;
    }

    .image {
      width: 100%;
      max-height: 875px;
    }

    .header-heading {
      margin-top: 5px;
    }    

    table {
      /* Repeats the header and footer on each page that the table is on. */
      -fs-table-paginate: paginate;
    }

    .sectionTable {
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
      page-break-inside:auto
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

  </style>
</head>
<body>

  <div id="header-content">

    <img class="logo-image" src="classpath:///document-assets/oga-logo.png"/>

    <h1 class="header-heading">${consentRef!"Pipeline Works Authorisations document"}</h1>

  </div>

  <#if showWatermark>
    <watermark>TEST DOCUMENT</watermark>
  </#if>

  ${sectionHtml?no_esc}

</body>
</html>