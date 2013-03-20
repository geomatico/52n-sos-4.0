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
package org.n52.sos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Utility class for file handling
 */
public class FileIOHelper {

    private static final byte LINE_FEED = (byte) '\n';
    private static final byte CARRIAGE_RETURN = (byte) '\r';
    private static final String READ_MODE = "r";

    /**
     * Loads a file and returns an InputStream

     *
     *
     * @param file File to load
     *
     * @return InputStream of the file
     *
     * @throws OwsExceptionReport If and error occurs;
     */
    public static InputStream loadInputStreamFromFile(File file) throws OwsExceptionReport {
        InputStream is;
        try {
            is = new FileInputStream(file);
            return is;
        } catch (FileNotFoundException fnfe) {
            throw new GenericThrowableWrapperException(fnfe);
        }
    }

    /* TODO refactor this */
    public static List<String> tail(File file, int lines) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, READ_MODE);
            final long length = file.length() - 1;
            ArrayList<String> out = new ArrayList<String>(lines);
            StringBuilder sb = new StringBuilder();
            byte prev = -1;
            for (long pos = length; pos != -1; pos--) {
                raf.seek(pos);
                byte b = raf.readByte();
                try {
                    if (b == CARRIAGE_RETURN) {
                        continue;
                    } else if (b == LINE_FEED) {
                        if (pos == length 
                         || pos == length-1 
                         || prev == CARRIAGE_RETURN) {
                            continue;
                        }
                        out.add(sb.reverse().toString());
                        sb = null;
                        if (out.size() == lines) {
                            break;
                        } else {
                            sb = new StringBuilder();
                        }
                    } else {
                        sb.append((char) b);
                    }
                } finally {
                    prev = b;
                }
            }
            if (sb != null) {
                out.add(sb.reverse().toString());
            }
            Collections.reverse(out);
            return out;
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    private FileIOHelper() {
    }
}
