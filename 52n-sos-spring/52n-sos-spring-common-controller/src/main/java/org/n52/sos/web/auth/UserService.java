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
package org.n52.sos.web.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;

import javax.servlet.ServletContext;

import org.n52.sos.config.AdministratorUser;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.service.ConfigurationException;
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
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private ServletContext context;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UsernamePasswordAuthenticationToken authenticate(Authentication authentication) throws
            AuthenticationException {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        AdministratorUser user = authenticate((String) auth.getPrincipal(), (String) auth.getCredentials());
        return new UsernamePasswordAuthenticationToken(new AdministratorUserPrinciple(user), null, Collections
                .singleton(new AdministratorAuthority()));
    }

    public AdministratorUser authenticate(String username, String password) throws AuthenticationException {
        AdministratorUser user;
        
        if (username == null || password == null) {
            throw new BadCredentialsException("Bad Credentials");
        }

        try {
            user = SettingsManager.getInstance().getAdminUser(username);
        } catch (Exception ex) {
            log.error("Error querying admin", ex);
            throw new BadCredentialsException("Bad Credentials");
        }
        
        if (user == null) {
            throw new BadCredentialsException("Bad Credentials");
        }

        if (!username.equals(user.getUsername()) || !getPasswordEncoder().matches(password, user.getPassword())) {
            throw new BadCredentialsException("Bad Credentials");
        }

        return user;
    }

    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }

    public AdministratorUser createAdmin(String username, String password) {
        try {
            return SettingsManager.getInstance().createAdminUser(username, getPasswordEncoder().encode(password));
        } catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public void setAdminUserName(AdministratorUser user, String name) {
        try {
            SettingsManager.getInstance().saveAdminUser(user.setUsername(name));
        } catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public void setAdminPassword(AdministratorUser user, String password) {
        try {
            SettingsManager.getInstance().saveAdminUser(user.setPassword(getPasswordEncoder().encode(password)));
        } catch (Exception ex) {
            log.error("Error saving admin", ex);
            throw new RuntimeException(ex);
        }
    }

    public AdministratorUser getAdmin(String username) throws ConfigurationException {
        try {
            return SettingsManager.getInstance().getAdminUser(username);
        } catch (ConnectionProviderException e) {
           throw new ConfigurationException(e);
        }
    }

    public AdministratorUser getAdmin(Principal user) throws ConfigurationException {
        try {
            return SettingsManager.getInstance().getAdminUser(user.getName());
        } catch (ConnectionProviderException e) {
            throw new ConfigurationException(e);
        }
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

    private static class AdministratorAuthority implements GrantedAuthority {

        private static final long serialVersionUID = 5103351149817795492L;

        @Override
        public String getAuthority() {
            return ControllerConstants.ROLE_ADMIN;
        }
    }
    
    private class AdministratorUserPrinciple implements Principal, Serializable {
        private static final long serialVersionUID = 1L;

        private String username;
        
        AdministratorUserPrinciple(AdministratorUser user) {
            this.username = user.getUsername();
        }
        
        @Override
        public String getName() {
            return this.username;
        }
        
    }
}
