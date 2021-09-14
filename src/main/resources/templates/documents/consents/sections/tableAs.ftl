<#include '../../../pwaLayoutImports.ftl'>
<#import '../fragments/consentDocImage.ftl' as pwaConsentDocImage>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="drawingForTableAViews" type="java.util.List<uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView>"-->


<div>

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
        <#if tableAView.footnote?has_content>
          ${tableAView.footnote?no_esc}
        </#if>  
      </div>
    </#list>

    
    <div class="tableADrawing">
      <h4 class="govuk-heading-s">
        Reference Drawing: ${drawingForTableAView.drawingReference}
      </h4>
      <@pwaConsentDocImage.img src=drawingForTableAView.imageSource />
    </div>

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
        <th> Description of Main Component Parts of Pipeline </th>
        <th> Length (m) </th>
        <th> External Diameter (mm) </th>
        <th> Internal Diameter (mm) </th>
        <th> Wall Thickness (mm) </th>
        <th> Type of Insulation </th>
        <th> MAOP (barg) </th>
        <th> Product(s) to be Conveyed </th>
      </tr>
    </thead>

    <tbody>
      <#local plNumber = tableAView.headerRow.pipelineNumber />
      <@tableARow tableAView.headerRow plNumber/>
      <#list tableAView.identRows as identRow>
        <@tableARow identRow plNumber/>
      </#list>
    </tbody>

  </table>
</#macro>

<#macro tableARow tableARowView plNumber>
  <tr>
    <td class="headerRow"> ${plNumber} </td>
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

