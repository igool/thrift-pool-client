/**
 * thrift客户端
 */
package com.github.igool.thrift.client;


import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.google.common.base.Function;


/**
 * <p>ThriftClient interface.</p>
 *
 * @author igool
 * @version $Id: $Id
 */
public interface ThriftClient {

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @return a X object.
     */
    <X extends TServiceClient> X iface(Class<X> ifaceClass);

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param hash a int.
     * @return a X object.
     */
    <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash);

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param protocolProvider a {@link java.util.function.Function} object.
     * @param hash a int.
     * @return a X object.
     */
    <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash);

}
