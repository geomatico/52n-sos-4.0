package org.n52.sos.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringHelperTest {
    @Test
    public void should_sanitize_nc_name() {
        final String badNcName = "1i@want$to:be*an&nc(name!but{i}have~bad%characters";
        final String goodNcName = "_i_want_to_be_an_nc_name_but_i_have_bad_characters";
        assertEquals("StringHelper.sanitizeNcName didn't correctly sanitize the test string",
                goodNcName, StringHelper.sanitizeNcName(badNcName));
    }
}
