<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pwaPipelineViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView>" -->

<#macro tab pwaPipelineViews=[]>

  <table class="govuk-table">
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header" scope="col">Pipeline number</th>
        <th class="govuk-table__header" scope="col">Status</th>
        <th class="govuk-table__header" scope="col">Start location (WGS 84)</th>
        <th class="govuk-table__header" scope="col">End location (WGS 84)</th>
        <th class="govuk-table__header" scope="col">Length (metres)</th>        
      </tr>
    </thead>

    <tbody class="govuk-table__body">
    <#list pwaPipelineViews as pwaPipelineView>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
            ${pwaPipelineView.pipelineNumber}
        </td>
        <td class="govuk-table__cell">
            ${pwaPipelineView.status.getDisplayText()}
        </td>
        <td class="govuk-table__cell">
            ${pwaPipelineView.fromLocation}
            </br>
            <@pwaCoordinate.display coordinatePair=pwaPipelineView.fromCoordinates />
        </td>
        <td class="govuk-table__cell">
            ${pwaPipelineView.toLocation}
            </br>
            <@pwaCoordinate.display coordinatePair=pwaPipelineView.toCoordinates />
        </td>
        <td class="govuk-table__cell">
            ${pwaPipelineView.length}
        </td>
      </tr>
    </#list>    
    </tbody>

  </table>

  

</#macro>