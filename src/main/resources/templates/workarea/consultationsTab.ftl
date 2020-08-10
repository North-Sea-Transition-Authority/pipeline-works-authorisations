<#include '../layout.ftl'>
<#import '_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>

<#-- @ftlvariable name="workAreaPageView" type="uk.co.ogauthority.pwa.mvc.PageView<uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem>" -->

<#macro tab workAreaPageView>

    <#if workAreaPageView?has_content>

      <#if !workAreaPageView.getPageContent()?has_content>
          <@fdsInsetText.insetText>You have no open consultations.</@fdsInsetText.insetText>
      <#else>
        <table class="govuk-table">
          <thead class="govuk-table__head">
          <tr class="govuk-table__row">

            <@applicationWorkAreaItem.referenceColumnHeader />
            <th class="govuk-table__header" scope="col">Application type</th>
            <th class="govuk-table__header" scope="col">Master PWA reference</th>
              <@applicationWorkAreaItem.summaryColumnHeader />
            <th class="govuk-table__header" scope="col">Consultation status</th>

          </tr>
          </thead>
          <tbody class="govuk-table__body">
          <#list workAreaPageView.getPageContent() as item>

            <tr class="govuk-table__row">
              <td class="govuk-table__cell">
                <@applicationWorkAreaItem.referenceColumn item=item/>
              </td>
              <td class="govuk-table__cell"> ${item.getApplicationTypeDisplay()}</td>
              <td class="govuk-table__cell"> ${item.getMasterPwaReference()}</td>
              <td class="govuk-table__cell">
                <@applicationWorkAreaItem.summaryColumn item=item/>
              </td>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <@applicationWorkAreaItem.statusLabelListItem item=item />
                  <li>
                    Consultation due date: ${item.consultationRequestDeadlineDateTime}
                  </li>
                  <li>
                    Consultee: ${item.consulteeGroupName} ${item.consulteeGroupAbbr?has_content?then("(" + item.consulteeGroupAbbr + ")", "")}
                  </li>
                  <#if item.assignedResponderName?has_content>
                    <li>Responder: ${item.assignedResponderName}</li>
                  </#if>
                  <@applicationWorkAreaItem.fastTrackLabelListItem item=item />
                </ul>
              </td>
            </tr>
          </#list>

          </tbody>
        </table>

        <@fdsPagination.pagination pageView=workAreaPageView />

      </#if>

    </#if>

</#macro>