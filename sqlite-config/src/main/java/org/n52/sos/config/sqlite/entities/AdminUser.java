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
package org.n52.sos.config.sqlite.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.n52.sos.config.AdministratorUser;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Entity(name = "administrator_user")
public class AdminUser implements Serializable, AdministratorUser {
    private static final long serialVersionUID = -6073682567042001348L;
    public static final String USERNAME_PROPERTY = "username";
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = AdminUser.USERNAME_PROPERTY, unique = true)
    private String username;
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public AdminUser setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public AdminUser setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 67;
        int hash = 5;
        hash = prime * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = prime * hash + (this.username != null ? this.username.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AdminUser) {
            final AdminUser other = (AdminUser) obj;
            return (getId() == null ? other.getId() == null : getId().equals(other.getId()))
                   && (getUsername() == null ? other.getUsername() == null : getUsername().equals(other.getUsername()));
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[username=%s, hash=%s]", getClass().getSimpleName(), getUsername(), getPassword());
    }
}
