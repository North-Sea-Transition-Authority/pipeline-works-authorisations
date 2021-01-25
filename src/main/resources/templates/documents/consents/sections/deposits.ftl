<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="depositTableRowViews" type="java.util.List<uk.co.ogauthority.pwa.service.documents.views.DepositTableRowView>"-->


<div id="depositsTableSection">

  <h2 class="govuk-heading-l">${sectionName}</h2>

  <table class="sectionTable">
    <thead>
      <tr>
        <th rowSpan="2"> Pipeline number </th>
        <th rowSpan="2"> Proposed date </th>
        <th rowSpan="2"> Type & size of materials </th>
        <th rowSpan="2"> Quantity </th>
        <th colspan="2"> Location of deposit </th>
        <th rowSpan="2"> Drawing numbers </th>
      </tr>
      <tr>
        <th> From: </th>
        <th> To: </th>
      </tr>
    </thead>

    <tbody>
      <#list depositTableRowViews as depositTableRowView>
        <tr>
          <td> ${depositTableRowView.pipelineNumber} </td>
          <td> ${depositTableRowView.proposedDate} </td>
          <td class="materialTypeAndSize"> ${depositTableRowView.typeAndSizeOfMaterials} </td>
          <td> ${depositTableRowView.quantity} </td>
          <td class="coordinateTableCell"> <@pwaCoordinate.display coordinatePair=depositTableRowView.fromCoordinates/> </td>
          <td class="coordinateTableCell"> <@pwaCoordinate.display coordinatePair=depositTableRowView.toCoordinates/> </td>

          <td>
            <ul class="drawingNumberList">
              <#list depositTableRowView.drawingNumbers as drawingNumber>
                <li> ${drawingNumber} </li>
              </#list>
            </ul>
          </td>
        </tr>
      </#list>
    </tbody>
  </table>

</div>