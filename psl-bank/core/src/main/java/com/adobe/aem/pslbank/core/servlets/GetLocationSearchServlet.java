package com.adobe.aem.pslbank.core.servlets;

import com.adobe.aem.pslbank.core.utils.Network;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

import static com.adobe.aem.pslbank.core.constants.AppConstant.*;

@Component(service = Servlet.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Servlet to get Location Address",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/psl-bank/locationsearch-query",})
public class GetLocationSearchServlet extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 632L;

    private static final Logger log = LoggerFactory.getLogger(GetLocationSearchServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        try {
            log.info("Reading the data from the webservice");
            String URL= GeoURL_Prefix + request.getParameter("keyword")
                    + GeoURL_Suffix + "country=in&" + GeoAPIKey;
            String responseString = Network.readJson(URL);
            String coords ="";
            String placeName ="";
            String[] csv = responseString.split(",\"");
            String key;
            for (int i = 0; i < csv.length; i++) {
                key = new String(csv[i]);
                key = key.replace("\"", "");

                if (key.startsWith("center")) {
                    coords = key.split(":")[1];
                    break;
                }
            }
            URL= GeoURL_Prefix + coords.substring(1,coords.length()-1)
                    + GeoURL_Suffix+ GeoAPIKey;
            responseString = Network.readJson(URL);
            csv = responseString.split(",\"");
            for (int i = 0; i < csv.length; i++) {
                key = new String(csv[i]);
                key = key.replace("\"", "");
                if (key.startsWith("place_name")) {
                    placeName = key.split(":")[1];
                    break;
                }
            }
            response.getWriter().println(placeName);

        } catch (Exception e) {

            log.error(e.getMessage(), e);
        }
    }
}
