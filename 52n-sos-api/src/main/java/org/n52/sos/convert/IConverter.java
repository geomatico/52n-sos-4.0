package org.n52.sos.convert;

import java.util.List;


public interface IConverter<T, S> {
    
    public List<ConverterKeyType> getConverterKeyTypes();
    
    public T convert(S objectToConvert) throws ConverterException;

}
