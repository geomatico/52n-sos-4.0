/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.web.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.GeneralQueryDAO;
import org.n52.sos.exception.ows.concrete.NoImplementationFoundException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.web.ControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminDatasourceController extends AbstractDatasourceController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminDatasourceController.class);
    private static final String TEST_OFFERING_PREFIX = "test_offering";
    private static final String ROWS = "rows";
    private static final String NAMES = "names";
    private static final String IS_TEST_DATA_SET_INSTALLED = "testDataInstalled";
    private static final String SUPPORTS_TEST_DATA = "supportsTestData";
    private static final String SUPPORTS_CLEAR = "supportsClear";
    private static final String SUPPORTS_DELETE_DELETED =
            "supportsDeleteDeleted";
    private ServiceLoader<GeneralQueryDAO> daoServiceLoader = ServiceLoader.load(GeneralQueryDAO.class);

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE)
    public ModelAndView index() throws SQLException {
        Map<String, Object> model = CollectionHelper.map();
        boolean supportsTestData = getDatasource().supportsTestData();
        model.put(SUPPORTS_TEST_DATA, supportsTestData);
        model.put(SUPPORTS_CLEAR, getDatasource().supportsClear());
        model.put(SUPPORTS_DELETE_DELETED,
                  daoServiceLoader.iterator().hasNext());
        model.put(IS_TEST_DATA_SET_INSTALLED,
                  supportsTestData ? getDatasource().isTestDataPresent(getSettings()) : false);
        return new ModelAndView(ControllerConstants.Views.ADMIN_DATASOURCE, model);
    }

    //FIXME move this to AbstrachHibernateDatasource
    private boolean checkCacheForTestDataSet() {
        Collection<String> offerings = Configurator.getInstance().getCache().getOfferings();
        for (String offering : offerings) {
            if (offering.startsWith(TEST_OFFERING_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_EXECUTE, method = RequestMethod.POST)
    public String processQuery(@RequestBody String querySQL) {
        try {
            String q = URLDecoder.decode(querySQL, "UTF-8");
            LOG.info("Query: {}", q);
            GeneralQueryDAO dao = daoServiceLoader.iterator().next();
            GeneralQueryDAO.QueryResult rs = dao.query(q);

            JSONObject j = new JSONObject();
            if (rs.getMessage() != null) {
                j.put(rs.isError() ? "error" : "message", rs.getMessage());
                return j.toString();
            }

            JSONArray names = new JSONArray(rs.getColumnNames());
            JSONArray rows = new JSONArray();
            for (GeneralQueryDAO.Row row : rs.getRows()) {
                rows.put(new JSONArray(row.getValues()));
            }

            return new JSONObject()
                    .put(ROWS, rows)
                    .put(NAMES, names)
                    .toString();
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Could not decode String", ex);
            return "Could not decode String: " + ex.getMessage();
        } catch (Exception ex) {
            LOG.error("Query unsuccesfull.", ex);
            return "Query unsuccesfull. Cause: " + ex.getMessage();
        }
    }

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(UnsupportedOperationException.class)
	public String onError(UnsupportedOperationException e) {
		return "The operation is not supported.";
	}
	
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_REMOVE_TEST_DATA, method = RequestMethod.POST)
    public void deleteTestData() throws OwsExceptionReport,
                                        ConnectionProviderException {
        if (getDatasource().supportsTestData()) {
            LOG.info("Removing test data set.");
            getDatasource().removeTestData(getSettings());
            updateCache();
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_CREATE_TEST_DATA, method = RequestMethod.POST)
    public void createTestData() throws OwsExceptionReport,
                                        ConnectionProviderException {
        
        if (getDatasource().supportsTestData()) {
            LOG.info("Inserting test data set.");
            getDatasource().insertTestData(getSettings());
            updateCache();
        }
        
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_CLEAR,
                    method = RequestMethod.POST)
    public void clearDatasource() throws OwsExceptionReport,
                                         ConnectionProviderException {
        if (getDatasource().supportsClear()) {
            LOG.info("Clearing database contents.");
            getDatasource().clear(getSettings());
            updateCache();
        }
    }
}
