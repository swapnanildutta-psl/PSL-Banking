package com.adobe.aem.pslbank.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

import static com.day.cq.wcm.api.NameConstants.NT_PAGE;

@Component(service = Servlet.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Search Servlet",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/psl-bank/search-query",
        "sling.servlet.resourceTypes=" + "psl-bank/components/page"
})
public class SearchServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 632L;

    private static final Logger log = LoggerFactory.getLogger(SearchServlet.class);

    @Reference
    private QueryBuilder builder;

    private Session session;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        try {
            log.info("----------< Executing Query Builder Servlet >----------");
            String searchText = request.getParameter("searchText");
            String relativePath = request.getParameter("relativePath");

            log.info("Search term is: {}", searchText);
            ResourceResolver resourceResolver = request.getResourceResolver();
            session = resourceResolver.adaptTo(Session.class);

            Map<String, String> predicate = new HashMap<>();
            predicate.put("path", relativePath);
            predicate.put("type", NT_PAGE);
            predicate.put("fulltext", searchText);

            Query query = builder.createQuery(PredicateGroup.create(predicate), session);
            query.setStart(0);
            query.setHitsPerPage(20);

            SearchResult searchResult = query.getResult();
            if (searchResult.getHits().isEmpty()){
                response.sendError(SlingHttpServletResponse.SC_NOT_FOUND, "No Result Found");
            }
            else{
                for (Hit hit : searchResult.getHits()) {
                    String path = hit.getPath();
                    response.getWriter().println(path);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
}