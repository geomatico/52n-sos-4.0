package org.n52.sos.web.admin;

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


import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.ServiceLoader;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGeneralQueryDao;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.ControllerConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminDatabaseController extends AbstractAdminController {

    public static final String TEST_OFFERING_PREFIX = "test_offering";
    private static final String ROWS = "rows";
    private static final String NAMES = "names";
    private static final String IS_TEST_DATA_SET_INSTALLED_MODEL_ATTRIBUTE = "IS_TEST_DATA_SET_INSTALLED_MODEL_ATTRIBUTE";
    private ServiceLoader<IGeneralQueryDao> daoServiceLoader = ServiceLoader.load(IGeneralQueryDao.class);

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE)
    public ModelAndView index() throws SQLException {
        return new ModelAndView(ControllerConstants.Views.ADMIN_DATABASE,
                                IS_TEST_DATA_SET_INSTALLED_MODEL_ATTRIBUTE,
                                Boolean.valueOf(checkCacheForTestDataSet()));
    }

    private boolean checkCacheForTestDataSet() {
        Collection<String> offerings = Configurator.getInstance().getCapabilitiesCacheController().getOfferings();
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
            log.info("Query: {}", q);
            IGeneralQueryDao dao = daoServiceLoader.iterator().next();
            IGeneralQueryDao.QueryResult rs = dao.query(q);

            JSONObject j = new JSONObject();
            if (rs.getMessage() != null) {
                j.put(rs.isError() ? "error" : "message", rs.getMessage());
                return j.toString();
            }

            JSONArray names = new JSONArray(rs.getColumnNames());
            JSONArray rows = new JSONArray();
            for (IGeneralQueryDao.Row row : rs.getRows()) {
                rows.put(new JSONArray(row.getValues()));
            }

            return new JSONObject().put(ROWS, rows).put(NAMES, names).toString();
        } catch (UnsupportedEncodingException ex) {
            log.error("Could not decode String", ex);
            return "Could not decode String: " + ex.getMessage();
        } catch (Exception ex) {
            log.error("Query unsuccesfull.", ex);
            return "Query unsuccesfull. Cause: " + ex.getMessage();
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_REMOVE_TEST_DATA, method = RequestMethod.POST)
    public void removeTestData() throws SQLException, OwsExceptionReport, FileNotFoundException, ConnectionProviderException {
        log.info("Removing test data set.");
        executeSqlFile(ControllerConstants.REMOVE_TEST_DATA_SQL_FILE);
        Configurator.getInstance().getCapabilitiesCacheController().updateCacheFromDB();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_CREATE_TEST_DATA, method = RequestMethod.POST)
    public void createTestData() throws SQLException, OwsExceptionReport, FileNotFoundException, ConnectionProviderException {
        log.info("Inserting test data set.");
        executeSqlFile(ControllerConstants.INSERT_TEST_DATA_SQL_FILE);
        Configurator.getInstance().getCapabilitiesCacheController().updateCacheFromDB();
    }
}