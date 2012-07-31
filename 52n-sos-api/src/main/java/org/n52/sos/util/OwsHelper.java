package org.n52.sos.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwsHelper {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OwsHelper.class);

    /**
     * Sets the first character to UpperCase.
     * 
     * @param name
     *            String to be modified.
     * @return Modified string.
     */
    public static String refactorOpsName(String name) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);

    }
    
    /**
     * method checks whether this SOS supports the requested versions
     * 
     * @param versions
     *            the requested versions of the SOS
     * 
     * @throws OwsExceptionReport
     *             if this SOS does not support the requested versions
     */
    public static List<String> checkAcceptedVersionsParameter(List<String> versions, Set<String> supportedVersions) throws OwsExceptionReport {

        List<String> validVersions = new ArrayList<String>();
        if (versions != null) {

            for (String version : versions) {
                if (supportedVersions.contains(version)) {
                    validVersions.add(version);
                }
            }
            if (validVersions.isEmpty()) {
                String exceptionText =
                        "The parameter '" + SosConstants.GetCapabilitiesParams.AcceptVersions.name() + "'"
                                + " does not contain a supported Service version!";
                LOGGER.error(exceptionText);
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.VersionNegotiationFailed,
                        SosConstants.GetCapabilitiesParams.AcceptVersions.name(), exceptionText);
                throw se;
            }
            return validVersions;
        } else {
            OwsExceptionReport owse =
                    Util4Exceptions.createMissingParameterValueException(SosConstants.GetCapabilitiesParams.AcceptVersions
                            .name());
            LOGGER.error("checkAcceptedVersionsParameters", owse);
            throw owse;
        }
    }

    /**
     * method checks whether this SOS supports the single requested version
     * 
     * @param version
     *            the requested version of the SOS
     * @throws OwsExceptionReport
     *             if this SOS does not support the requested versions
     */
    public static void checkSingleVersionParameter(String version, Set<String> supportedVersions) throws OwsExceptionReport {

        // if version is incorrect, throw exception
        if (version == null
                || !supportedVersions.contains(version)) {

            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The parameter '");
            exceptionText.append(OWSConstants.RequestParams.version.name());
            exceptionText.append("'  does not contain version(s) supported by this Service: '");
            for (String supportedVersion : supportedVersions) {
                exceptionText.append(supportedVersion);
                exceptionText.append("', '");
            }
            exceptionText.delete(exceptionText.lastIndexOf("', '"), exceptionText.length());
            exceptionText.append("'!");
            LOGGER.error("The accepted versions parameter is incorrect.");
            OwsExceptionReport owse = new OwsExceptionReport();
            owse.addCodedException(OwsExceptionCode.InvalidParameterValue,
                    OWSConstants.RequestParams.version.name(), exceptionText.toString());

            throw owse;
        }
    }

    /**
     * method checks, whether the passed string containing the requested
     * versions of the SOS contains the versions, the 52n SOS supports
     * 
     * @param versionsString
     *            comma seperated list of requested service versions
     * @throws OwsExceptionReport
     *             if the versions list is empty or no matching version is
     *             contained
     */
    public static void checkAcceptedVersionsParameter(String versionsString, Set<String> supportedVersions) throws OwsExceptionReport {
        // check acceptVersions
        if (versionsString != null && !versionsString.equals("")) {
            String[] versionsArray = versionsString.split(",");
            checkAcceptedVersionsParameter(Arrays.asList(versionsArray), supportedVersions);
        } else {
            OwsExceptionReport se =
                    Util4Exceptions.createMissingParameterValueException(SosConstants.GetCapabilitiesParams.AcceptVersions
                            .name());
            LOGGER.error("checkAcceptedVersionsParameter", se);
            throw se;
        }
    }
    
}
