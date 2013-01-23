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
package org.n52.sos.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.util.AbstractServiceLoaderRepository;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ConverterRepository extends AbstractServiceLoaderRepository<IConverter> {
	private final Map<ConverterKeyType, IConverter<?, ?>> converter = new HashMap<ConverterKeyType, IConverter<?, ?>>(0);

	public ConverterRepository() throws ConfigurationException {
		super(IConverter.class, false);
		load(false);
	}

	@Override
	protected void processImplementations(Set<IConverter> converter) throws ConfigurationException {
		this.converter.clear();
		for (IConverter<?, ?> aConverter : converter) {
			for (ConverterKeyType converterKeyType : aConverter.getConverterKeyTypes()) {
				this.converter.put(converterKeyType, aConverter);
			}
		}
		// TODO check for encoder/decoder used by converter
	}

    public <T,F> IConverter<T,F> getConverter(String fromNamespace, String toNamespace) {
        return getConverter(new ConverterKeyType(fromNamespace, toNamespace));
    }

    public <T,F> IConverter<T,F> getConverter(ConverterKeyType key) {
        return (IConverter<T,F>) converter.get(key);
    }
}
