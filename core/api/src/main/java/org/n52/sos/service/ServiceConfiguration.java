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

import org.n52.sos.config.SettingsManager;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Validation;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
@Configurable
public class ServiceConfiguration {
	private static ServiceConfiguration instance;
	
    /**
     * character encoding for responses.
     */
    private String characterEncoding;
    private String defaultOfferingPrefix;
    private String defaultProcedurePrefix;
    private String defaultObservablePropertyPrefix;
    private String defaultFeaturePrefix;
    private boolean useDefaultPrefixes;
    private boolean encodeFullChildrenInDescribeSensor;
    private boolean useHttpStatusCodesInKvpAndPoxBinding;

    /**
     * @return Returns a singleton instance of the ServiceConfiguration.
     */
    public static ServiceConfiguration getInstance() {
    	if (instance == null) {
    		SettingsManager.getInstance().configure(instance = new ServiceConfiguration());
    	}
    	return instance;
    }    
    
    /**
     * private constructor for singleton
     */
    private ServiceConfiguration() {
    }

    /**
     * date format of gml.
     */
    private String gmlDateFormat;
    /**
     * minimum size to gzip responses.
     */
    private int minimumGzipSize;
    /**
     * URL of this service.
     */
    private String serviceURL;
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
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeparator() {
        return tokenSeparator;
    }

    @Setting(MiscSettings.TOKEN_SEPARATOR)
    public void setTokenSeparator(final String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Token separator", separator);
        tokenSeparator = separator;
    }

    public String getTupleSeparator() {
        return tupleSeparator;
    }

    @Setting(MiscSettings.TUPLE_SEPARATOR)
    public void setTupleSeparator(final String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Tuple separator", separator);
        tupleSeparator = separator;
    }

    @Setting(CHARACTER_ENCODING)
    public void setCharacterEncoding(final String encoding) throws ConfigurationException {
        Validation.notNullOrEmpty("Character Encoding", encoding);
        characterEncoding = encoding;
        XmlOptionsHelper.getInstance().setCharacterEncoding(characterEncoding);
    }
    
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    public int getMinimumGzipSize() {
        return minimumGzipSize;
    }

    @Setting(MINIMUM_GZIP_SIZE)
    public void setMinimumGzipSize(final int size) {
        minimumGzipSize = size;
    }

    public String getDefaultOfferingPrefix() {
        return defaultOfferingPrefix;
    }

    @Setting(DEFAULT_OFFERING_PREFIX)
    public void setDefaultOfferingPrefix(final String prefix) {
        defaultOfferingPrefix = prefix;
    }

    public String getDefaultProcedurePrefix() {
        return defaultProcedurePrefix;
    }

    @Setting(DEFAULT_OBSERVABLEPROPERTY_PREFIX)
    public void setDefaultObservablePropertyPrefix(final String prefix) {
        defaultObservablePropertyPrefix = prefix;
    }

    public String getDefaultObservablePropertyPrefix() {
        return defaultObservablePropertyPrefix;
    }

    @Setting(DEFAULT_PROCEDURE_PREFIX)
    public void setDefaultProcedurePrefix(final String prefix) {
        defaultProcedurePrefix = prefix;
    }

    public String getDefaultFeaturePrefix() {
        return defaultFeaturePrefix;
    }

    @Setting(DEFAULT_FEATURE_PREFIX)
    public void setDefaultFeaturePrefix(final String prefix) {
        defaultFeaturePrefix = prefix;
    }

    public boolean isUseDefaultPrefixes() {
        return useDefaultPrefixes;
    }

    @Setting(USE_DEFAULT_PREFIXES)
    public void setUseDefaultPrefixes(final boolean prefix) {
        useDefaultPrefixes = prefix;
    }

    public boolean isEncodeFullChildrenInDescribeSensor() {
    	return encodeFullChildrenInDescribeSensor;
    }

    @Setting(ENCODE_FULL_CHILDREN_IN_DESCRIBE_SENSOR)
    public void setEncodeFullChildrenInDescribeSensor(final boolean encodeFullChildrenInDescribeSensor) {
    	this.encodeFullChildrenInDescribeSensor = encodeFullChildrenInDescribeSensor;
    }
    
    /**
     * @return the supportsQuality
     */
    //HibernateObservationUtilities
    public boolean isSupportsQuality() {
        return supportsQuality;
    }

//    @Setting(SUPPORTS_QUALITY)
//    public void setSupportsQuality(final boolean supportsQuality) {
//        this.supportsQuality = supportsQuality;
//    }

    @Setting(GML_DATE_FORMAT)
    public void setGmlDateFormat(final String format) {
        // TODO remove variable?
        gmlDateFormat = format;
        DateTimeHelper.setResponseFormat(gmlDateFormat);
    }

    public boolean isUseHttpStatusCodesInKvpAndPoxBinding() {
        return useHttpStatusCodesInKvpAndPoxBinding;
    }

    @Setting(HTTP_STATUS_CODE_USE_IN_KVP_POX_BINDING)
    public void setUseHttpStatusCodesInKvpAndPoxBinding(final boolean useHttpStatusCodesInKvpAndPoxBinding) {
        Validation.notNull(HTTP_STATUS_CODE_USE_IN_KVP_POX_BINDING, useHttpStatusCodesInKvpAndPoxBinding);
        this.useHttpStatusCodesInKvpAndPoxBinding = useHttpStatusCodesInKvpAndPoxBinding;
    }

    /**
     * @return Returns the sensor description directory
     */
    //HibernateProcedureUtilities
    public String getSensorDir() {
        return sensorDirectory;
    }

    @Setting(SENSOR_DIRECTORY)
    public void setSensorDirectory(final String sensorDirectory) {
        this.sensorDirectory = sensorDirectory;
    }

    /**
     * Get service URL.
     *
     * @return the service URL
     */
    public String getServiceURL() {
        return serviceURL;
    }

    @Setting(SERVICE_URL)
    public void setServiceURL(final URI serviceURL) throws ConfigurationException {
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
        return srsNamePrefix;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V1)
    public void setSrsNamePrefixForSosV1(String prefix) {
        if (!prefix.endsWith(":") && !prefix.isEmpty()) {
            prefix += ":";
        }
        srsNamePrefix = prefix;
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
        return srsNamePrefixSosV2;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V2)
    public void setSrsNamePrefixForSosV2(String prefix) {
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix += "/";
        }
        srsNamePrefixSosV2 = prefix;
    }
    
}
