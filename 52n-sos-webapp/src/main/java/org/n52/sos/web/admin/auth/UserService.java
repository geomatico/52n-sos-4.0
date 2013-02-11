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
package org.n52.sos.web.admin.auth;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;

import org.n52.sos.ds.IAdminUserDao;
import org.n52.sos.service.AdminUser;
import org.n52.sos.web.ControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/* TODO make this serializable */
public class UserService implements AuthenticationProvider, Serializable {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final long serialVersionUID = 1L;

    @Autowired
    private ServletContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ServiceLoader<IAdminUserDao> daoServiceLoader = ServiceLoader.load(IAdminUserDao.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        String username = String.valueOf(auth.getPrincipal());
        String password = String.valueOf(auth.getCredentials());

        AdminUser user;
        try {
            user = getAdmin();
        }
        catch (Exception ex) {
            log.error("Error querying admin", ex);
            throw new BadCredentialsException("Bad Credentials");
        }

        if ( !username.equals(user.getUsername()) || !getPasswordEncoder().matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Bad Credentials");
        }

        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            private static final long serialVersionUID = 5103351149817795492L;

            @Override
            public String getAuthority() {
                return ControllerConstants.ROLE_ADMIN;
            }
        };

        return new UsernamePasswordAuthenticationToken(user, null, Collections.singleton(grantedAuthority));
    }

    @Override
    public boolean supports(Class< ? > type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }

    public void saveAdmin(AdminUser admin) {
        try {
            IAdminUserDao dao = daoServiceLoader.iterator().next();
            dao.saveAdminUser(encode(admin));
        }
        catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    private AdminUser encode(AdminUser admin) {
        return new AdminUser(admin.getUsername(), getPasswordEncoder().encode(admin.getPasswordHash()));
    }

    public void saveAdmin(AdminUser admin, Properties properties) {
        try {
            IAdminUserDao dao = daoServiceLoader.iterator().next();
            dao.initialize(properties);
            dao.saveAdminUser(encode(admin));
        }
        catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public void setAdminUserName(String name) {
        try {
            IAdminUserDao dao = daoServiceLoader.iterator().next();
            dao.setAdminUserName(name);
        }
        catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public void setAdminPassword(String password) {
        try {
            IAdminUserDao dao = daoServiceLoader.iterator().next();
            dao.setAdminPassword(getPasswordEncoder().encode(password));
        }
        catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public AdminUser getAdmin() throws SQLException {
        IAdminUserDao dao = daoServiceLoader.iterator().next();
        return dao.getAdminUser();
    }

    public ServletContext getContext() {
        return context;
    }

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
