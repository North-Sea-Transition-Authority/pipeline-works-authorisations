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
          <td> ${depositTableRowView.pipelineNumbers} </td>
          <td> ${depositTableRowView.proposedDate} </br> ${depositTableRowView.depositReference}</td>
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

  <p>
    This Consent authorises only Deposits exactly as described, up to the maximum quantities specified in column 4 to be laid, in the positions listed and within the period stated within the Table - nothing else can be laid.  
    If anything different to what has been authorised within this Consent is required you must have prior consent from OGA before it can be laid.
  </p>

  <#if depositFootnotes?has_content>
    <p> 
      <ul class="govuk-list">
        <#list depositFootnotes as depositFootnote>
          <li> ${depositFootnote} </li>
        </#list>
      </ul>
    </p>
  </#if>

</div>