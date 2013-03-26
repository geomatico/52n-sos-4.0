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

package org.n52.sos.service;

import static org.n52.sos.service.MiscSettings.*;
import static org.n52.sos.service.ServiceSettings.*;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Validation;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Configurable
public class ServiceConfiguration {

    /**
     * character encoding for responses.
     */
    private String characterEncoding;
    /**
     * Map with indicator and name of additional config files for modules.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private Map<String, String> configFileMap = new HashMap<String, String>(0);

    private String defaultOfferingPrefix;
    
    private String defaultProcedurePrefix;
    
    private String defaultObservablePropertyPrefix;
    
    private String defaultFeaturePrefix;
    
    private boolean useDefaultPrefixes;
    /**
     * date format of gml.
     */
    private String gmlDateFormat;
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private int lease;
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private int maxGetObsResults;
    /**
     * minimum size to gzip responses.
     */
    private int minimumGzipSize;
    /**
     * URL of this service.
     */
    private String serviceURL;
    /**
     * boolean, indicates if duplicate observation should be silently ignored during insertion If set to false,
     * duplicate observations trigger an exception.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private boolean skipDuplicateObservations = false;
    /**
     * directory of sensor descriptions in SensorML format.
     */
    private String sensorDirectory;
    /**
     * Prefix URN for the spatial reference system.
     */
    private String srsNamePrefix;
    /**
     * prefix URN for the spatial reference system.
     */
    private String srsNamePrefixSosV2;
    /**
     * boolean indicates, whether SOS supports quality information in observations.
     */
    private boolean supportsQuality = true;
    /**
     * token separator for result element.
     */
    private String tokenSeparator;
    /**
     * tuple separator for result element.
     */
    private String tupleSeparator;
    /**
     * decimal separator for result element.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private String decimalSeparator;

    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeparator() {
        return this.tokenSeparator;
    }

    @Setting(MiscSettings.TOKEN_SEPARATOR)
    public void setTokenSeparator(String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Token separator", separator);
        this.tokenSeparator = separator;
    }

    public String getTupleSeparator() {
        return this.tupleSeparator;
    }

    @Setting(MiscSettings.TUPLE_SEPARATOR)
    public void setTupleSeparator(String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Tuple separator", separator);
        this.tupleSeparator = separator;
    }

    /**
     * @return the characterEncoding
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Setting(CHARACTER_ENCODING)
    public void setCharacterEncoding(String encoding) throws ConfigurationException {
        Validation.notNullOrEmpty("Character Encoding", encoding);
        this.characterEncoding = encoding;
        XmlOptionsHelper.getInstance().setCharacterEncoding(characterEncoding);
    }

    /**
     * @return the configFileMap
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public Map<String, String> getConfigFileMap() {
        return Collections.unmodifiableMap(configFileMap);
    }

    /**
     * @param configurationFiles
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(CONFIGURATION_FILES)
    public void setConfigurationFiles(String configurationFiles) {
        if (configurationFiles != null && !configurationFiles.isEmpty()) {
            for (String kvp : configurationFiles.split(";")) {
                String[] keyValue = kvp.split(" ");
                this.configFileMap.put(keyValue[0], keyValue[1]);
            }
        } else {
            this.configFileMap.clear();
        }
    }

    /**
     * Returns the default decimal seperator for results.
     * <p/>
     * @return decimal separator.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getDecimalSeparator() {
        return this.decimalSeparator;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(DECIMAL_SEPARATOR)
    public void setDecimalSeperator(String seperator) throws ConfigurationException {
        Validation.notNullOrEmpty("Decimal seperator", seperator);
        this.decimalSeparator = seperator;
    }

    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    public int getMinimumGzipSize() {
        return this.minimumGzipSize;
    }

    @Setting(MINIMUM_GZIP_SIZE)
    public void setMinimumGzipSize(int size) {
        this.minimumGzipSize = size;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getMaxGetObsResults() {
        return this.maxGetObsResults;
    }

    /**
     * @param maxResults
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(MAX_GET_OBSERVATION_RESULTS)
    public void setMaxGetObservationResults(int maxResults) {
        this.maxGetObsResults = maxResults;
    }


    public String getDefaultOfferingPrefix() {
        return this.defaultOfferingPrefix;
    }


    @Setting(DEFAULT_OFFERING_PREFIX)
    public void setDefaultOfferingPrefix(String prefix) {
        this.defaultOfferingPrefix = prefix;
    }
    
    public String getDefaultProcedurePrefix() {
        return this.defaultProcedurePrefix;
    }
    
    @Setting(DEFAULT_OBSERVABLEPROPERTY_PREFIX)
    public void setDefaultObservablePropertyPrefix(String prefix) {
        this.defaultObservablePropertyPrefix = prefix;
    }
    
    public String getDefaultObservablePropertyPrefix() {
        return this.defaultObservablePropertyPrefix;
    }


    @Setting(DEFAULT_PROCEDURE_PREFIX)
    public void setDefaultProcedurePrefix(String prefix) {
        this.defaultProcedurePrefix = prefix;
    }
    
    public String getDefaultFeaturePrefix() {
        return this.defaultFeaturePrefix;
    }


    @Setting(DEFAULT_FEATURE_PREFIX)
    public void setDefaultFeaturePrefix(String prefix) {
        this.defaultFeaturePrefix = prefix;
    }
    
    public boolean isUseDefaultPrefixes() {
        return this.useDefaultPrefixes;
    }


    @Setting(USE_DEFAULT_PREFIXES)
    public void setUseDefaultPrefixes(boolean prefix) {
        this.useDefaultPrefixes = prefix;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getLease() {
        return this.lease;
    }

    /**
     * @param lease
     *
     * @throws ConfigurationException
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(LEASE)
    public void setLease(int lease) throws ConfigurationException {
        Validation.greaterZero("Lease", lease);
        this.lease = lease;
    }

    /**
     * @return true if duplicate observations should be skipped during insertion
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public boolean isSkipDuplicateObservations() {
        return this.skipDuplicateObservations;
    }

    /**
     * @param skip
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(SKIP_DUPLICATE_OBSERVATIONS)
    public void setSkipDuplicateObservations(boolean skip) {
        this.skipDuplicateObservations = skip;
    }

    /**
     * @return the supportsQuality
     */
    //HibernateObservationUtilities
    public boolean isSupportsQuality() {
        return this.supportsQuality;
    }

    @Setting(SUPPORTS_QUALITY)
    public void setSupportsQuality(boolean supportsQuality) {
        this.supportsQuality = supportsQuality;
    }

    /**
     * @return Returns the gmlDateFormat.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getGmlDateFormat() {
        return this.gmlDateFormat;
    }

    @Setting(GML_DATE_FORMAT)
    public void setGmlDateFormat(String format) {
        // TODO remove variable?
        this.gmlDateFormat = format;
        DateTimeHelper.setResponseFormat(this.gmlDateFormat);
    }

    /**
     * @return Returns the sensor description directory
     */
    //HibernateProcedureUtilities
    public String getSensorDir() {
        return this.sensorDirectory;
    }

    @Setting(SENSOR_DIRECTORY)
    public void setSensorDirectory(String sensorDirectory) {
        this.sensorDirectory = sensorDirectory;
    }

    /**
     * Get service URL.
     *
     * @return the service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    @Setting(SERVICE_URL)
    public void setServiceURL(URI serviceURL) throws ConfigurationException {
        Validation.notNull("Service URL", serviceURL);
        String url = serviceURL.toString();
        if (url.contains("?")) {
            url = url.split("[?]")[0];
        }
        this.serviceURL = url;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    /*
     * SosHelper
     * AbstractKvpDecoder
     * GmlEncoderv311
     * ITRequestEncoder
     */
    public String getSrsNamePrefix() {
        return this.srsNamePrefix;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V1)
    public void setSrsNamePrefixForSosV1(String prefix) {
        if (!prefix.endsWith(":") && !prefix.isEmpty()) {
            prefix += ":";
        }
        this.srsNamePrefix = prefix;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    /*
     * SosHelper
     * GmlEncoderv321
     * AbstractKvpDecoder
     * SosEncoderv100
     */
    public String getSrsNamePrefixSosV2() {
        return this.srsNamePrefixSosV2;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V2)
    public void setSrsNamePrefixForSosV2(String prefix) {
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix += "/";
        }
        this.srsNamePrefixSosV2 = prefix;
    }

}
