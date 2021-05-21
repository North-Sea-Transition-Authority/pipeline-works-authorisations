<#include '../layout.ftl'>
<#import '_asBuiltNotificationWorkAreaItem.ftl' as asBuiltNotificationWorkAreaItem>


<#macro tab workAreaPageView>

    <#if workAreaPageView?has_content>

        <#if !workAreaPageView.getPageContent()?has_content>
            <@fdsInsetText.insetText>You have no outstanding as-built notifications.</@fdsInsetText.insetText>
        <#else>
          <table class="govuk-table">
            <thead class="govuk-table__head">
            <tr class="govuk-table__row">
              <th class="govuk-table__header" scope="col">Application</th>
              <th class="govuk-table__header govuk-!-width-one-third" scope="col">Holder</th>
              <th class="govuk-table__header govuk-!-width-one-third" scope="col">Summary</th>
              <th class="govuk-table__header govuk-!-width-one-third" scope="col">Notification group status</th>
            </tr>
            </thead>
            <tbody class="govuk-table__body">
            <#list workAreaPageView.getPageContent() as item>
                <@asBuiltNotificationWorkAreaItem.workAreaItemContentRow item/>
            </#list>
            </tbody>
          </table>

            <@fdsPagination.pagination pageView=workAreaPageView />

        </#if>

    </#if>

</#macro>