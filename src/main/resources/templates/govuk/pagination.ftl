<#macro paginationControls pageView>
  <nav role="navigation" aria-label="Pagination">
    <div class="pagination__summary">Showing ${pageView.pageRangeBottomValue} &ndash; ${pageView.pageRangeTopValue} of ${pageView.totalElements} results</div>
    <ul class="pagination">
      <#if pageView.currentPage != 0>
        <li class="pagination__item"><a class="pagination__link govuk-link" href="<@spring.url pageView.urlForPage(pageView.currentPage - 1)/>" aria-label="Previous page"><span aria-hidden="true" role="presentation">&laquo;</span> Previous</a></li>
      </#if>
      <#list 0..pageView.totalPages - 1>
        <#items as pageIndex>
          <#if pageIndex == pageView.currentPage>
            <li class="pagination__item"><a class="pagination__link govuk-link current" href="#" aria-current="true" aria-label="Page ${pageIndex + 1}, current page">${pageIndex + 1}</a></li>
            <#else>
              <li class="pagination__item"><a class="pagination__link govuk-link" href="<@spring.url pageView.urlForPage(pageIndex)/>" aria-label="Page ${pageIndex + 1}">${pageIndex + 1}</a></li>
          </#if>
        </#items>
      </#list>
      <#if pageView.currentPage != pageView.totalPages - 1>
        <li class="pagination__item"><a class="pagination__link govuk-link" href="<@spring.url pageView.urlForPage(pageView.currentPage + 1)/>" aria-label="Next page">Next <span aria-hidden="true" role="presentation">&raquo;</span></a></li>
      </#if>
    </ul>
  </nav>
</#macro>