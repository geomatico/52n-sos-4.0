package org.n52.sos.encode;

public interface IObservationEncoder<S, T> extends IEncoder<S, T> {
    
    public boolean isSingleValueEncoding();

}
