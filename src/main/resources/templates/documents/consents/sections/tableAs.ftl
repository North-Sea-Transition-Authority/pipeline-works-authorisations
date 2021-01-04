<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="drawingForTableAViews" type="java.util.List<uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView>"-->


<div id="tableAsSection">

  <h2 class="govuk-heading-l">${sectionName}</h2>

  <#list drawingForTableAViews as drawingForTableAView>

    <#list drawingForTableAView.tableAViews as tableAView>
      <div class="tableAPage">
        <table class="tableAHeaderInfo">
          <tr>
            <td> PIPELINE NAME </td> <td> ${tableAView.pipelineName} </td>
          </tr>
           <tr>
            <td> PROJECT NAME </td> <td> ${drawingForTableAView.projectName} </td>
          </tr>
           <tr>
            <td> REFERENCE DRAWING </td> <td> ${drawingForTableAView.drawingReference} </td>
          </tr>
        </table>
        <@tableA tableAView/>
      </div>
    </#list>

  </#list>

</div>

<#macro tableA tableAView>
  <table class="tableA sectionTable">
    <thead>
      <tr>
        <th> Pipeline No </th>
        <th> Ident No </th>
        <th> From </th>
        <th> To </th>
        <th> Desc of Main Component Parts of Pipeline </th>
        <th> Length (m) </th>
        <th> Ext Dia (mm) </th>
        <th> Int Dia (mm) </th>
        <th> W.T. (mm) </th>
        <th> Type of Insulation </th>
        <th> M.A.O.P. </th>
        <th> Product to be Conveyed </th>
      </tr>
    </thead>

    <tbody>
      <@tableARow tableARowView=tableAView.headerRow totalRows=tableAView.totalRows isHeaderRow=true/>
      <#list tableAView.identRows as identRow>
        <@tableARow identRow/>
      </#list>
    </tbody>

  </table>
</#macro>


<#macro tableARow tableARowView totalRows=2 isHeaderRow=false>
  <tr>
    <#if isHeaderRow>
      <td class="headerRow" rowSpan="${totalRows}"> ${tableARowView.pipelineNumber!} </td>
    </#if>
    <td> ${tableARowView.identNumber!} </td>
    <td class="coordinateTableCell">
      ${tableARowView.fromLocation!} </br>
      <@pwaCoordinate.display coordinatePair=tableARowView.fromCoordinates/>
    </td>
    <td class="coordinateTableCell">
      ${tableARowView.toLocation!} </br>
      <@pwaCoordinate.display coordinatePair=tableARowView.toCoordinates/>
    </td>
    <td style="width: 120px;"> ${tableARowView.componentParts!} </td>
    <td> ${tableARowView.length!} </td>
    <td> ${tableARowView.externalDiameter!} </td>
    <td> ${tableARowView.internalDiameter!} </td>
    <td> ${tableARowView.wallThickness!} </td>
    <td> ${tableARowView.typeOfInsulation!} </td>
    <td> ${tableARowView.maop!} </td>
    <td> ${tableARowView.productsToBeConveyed!} </td>

  </tr>
</#macro>