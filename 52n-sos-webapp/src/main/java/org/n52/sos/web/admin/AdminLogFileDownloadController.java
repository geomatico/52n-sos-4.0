package org.n52.sos.web.admin;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.n52.sos.service.AbstractLoggingConfigurator;
import org.n52.sos.web.ControllerConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = ControllerConstants.Paths.ADMIN_LOGGING_FILE_DOWNLOAD)
public class AdminLogFileDownloadController {

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void downloadFile(HttpServletResponse response) throws IOException {
        InputStream is = null;
        try {
            is = AbstractLoggingConfigurator.getInstance().getLogFile();
            if (is == null) {
                throw new RuntimeException("Could not read log file.");
            }
            FileCopyUtils.copy(is, response.getOutputStream());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
