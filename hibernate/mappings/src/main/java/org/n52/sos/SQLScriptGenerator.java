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
package org.n52.sos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;

/**
 * Class to generate the create and drop scripts for different databases.
 * Currently supported spatial databases to create scripts - PostgreSQL/PostGIS
 * - Oracle - H2/GeoDB
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.0
 * 
 */
public class SQLScriptGenerator {

    private Dialect getDialect(int selction) throws Exception {
        switch (selction) {
        case 1:
            return new PostgisDialect();
        case 2:
            return new OracleSpatial10gDialect();
        case 3:
            return new GeoDBDialect();
        default:
            throw new Exception("The entered value is invalid!");
        }
    }

    private int getSelection() throws IOException {
        System.out.println("This SQL script generator supports:");
        System.out.println("1   PostGIS");
        System.out.println("2   Oracle");
        System.out.println("3   H2/GeoDB");
        System.out.println();
        System.out.println("Enter your selection: ");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String selection = null;
        selection = br.readLine();
        return Integer.parseInt(selection);
    }

    public static void main(String[] args) {
        try {
            SQLScriptGenerator sqlScriptGenerator = new SQLScriptGenerator();
            Configuration configuration = new Configuration().configure("/sos-hibernate.cfg.xml");
            int selection = sqlScriptGenerator.getSelection();
            Dialect dia = sqlScriptGenerator.getDialect(selection);
            // create script
            String[] create = configuration.generateSchemaCreationScript(dia);
            System.out.println();
            System.out.println("Scripts are created for: " + dia.toString());
            System.out.println();
            System.out.println("#######################################");
            System.out.println("##           Create-Script           ##");
            System.out.println("#######################################");
            System.out.println();
            for (String t : create) {
                System.out.println(t + ";");
            }
            // drop script
            String[] drop = configuration.generateDropSchemaScript(dia);
            System.out.println();
            System.out.println("#######################################");
            System.out.println("##            Drop-Script            ##");
            System.out.println("#######################################");
            System.out.println();
            for (String t : drop) {
                System.out.println(t + ";");
            }
            System.out.println();
            System.out.println("#######################################");
        } catch (IOException ioe) {
            System.out.println("ERROR: IO error trying to read your input!");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
