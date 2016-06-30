/**
 * 
 */
package com.github.igool.thrift.client.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.github.igool.thrift.client.ThriftClient;
import com.github.igool.thrift.client.exception.NoBackendException;
import com.github.igool.thrift.client.pool.ThriftConnectionPoolProvider;
import com.github.igool.thrift.client.pool.ThriftServerInfo;
import com.github.igool.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;
import com.github.igool.thrift.client.utils.ThriftClientUtils;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

/**
 * <p>
 * ThriftClientImpl class.
 * 使用jdk7改写了部分核心方法块
 * </p>
 *
 * @author w.vela
 * @author https://github.com/igool
 * @version $Id: $Id
 */
public class ThriftClientImpl implements ThriftClient {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    private final ThriftConnectionPoolProvider poolProvider;

    private final Supplier<List<ThriftServerInfo>> serverInfoProvider;

    /**
     * <p>
     * Constructor for ThriftClientImpl.
     * </p>
     *
     * @param serverInfoProvider provide service list
     */
    public ThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider) {
        this(serverInfoProvider, DefaultThriftConnectionPoolImpl.getInstance());
    }

    /**
     * <p>
     * Constructor for ThriftClientImpl.
     * </p>
     *
     * @param serverInfoProvider provide service list
     * @param poolProvider provide a pool
     */
    public ThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider,
            ThriftConnectionPoolProvider poolProvider) {
        this.poolProvider = poolProvider;
        this.serverInfoProvider = serverInfoProvider;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * iface.
     * </p>
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
        return iface(ifaceClass, ThriftClientUtils.randomNextInt());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * iface.
     * </p>
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash) {
       // return iface(ifaceClass, TCompactProtocol::new, hash);
    	return iface(ifaceClass, new  Function<TTransport, TProtocol>() {
			@Override
			public TProtocol apply(TTransport input) {
				// TODO Auto-generated method stub
	            return new TBinaryProtocol(input);
			}
        },hash);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * iface.
     * </p>
     */
    @SuppressWarnings("unchecked")
    @Override
    public <X extends TServiceClient> X iface(final Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash) {
        List<ThriftServerInfo> servers = serverInfoProvider.get();
        if (servers == null || servers.isEmpty()) {
            throw new NoBackendException();
        }
        hash = Math.abs(hash);
        hash = hash < 0 ? 0 : hash;
        final ThriftServerInfo selected = servers.get(hash % servers.size());
        logger.trace("get connection for [{}]->{} with hash:{}", ifaceClass, selected, hash);

        final TTransport transport = poolProvider.getConnection(selected);
        TProtocol protocol = protocolProvider.apply(transport);

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(ifaceClass);
/*        factory.setFilter(m -> ThriftClientUtils.getInterfaceMethodNames(ifaceClass).contains(
                m.getName()));*/
        //使用jdk7来改写，查找对应的方法
        factory.setFilter(new MethodFilter(){

			@Override
			public boolean isHandled(Method m) {
				// TODO Auto-generated method stub
				return ThriftClientUtils.getInterfaceMethodNames(ifaceClass).contains(m.getName());
			}});
        
        try {
            X x = (X) factory.create(new Class[] { org.apache.thrift.protocol.TProtocol.class },
                    new Object[] { protocol });
            //使用JDK7来改写代理的方法调用
            ((Proxy) x).setHandler(new MethodHandler(){

				@Override
				public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
					// TODO Auto-generated method stub
					 boolean success = false;
		                try {
		                    Object result = proceed.invoke(self, args);
		                    success = true;
		                    return result;
		                } finally {
		                    if (success) {
		                        poolProvider.returnConnection(selected, transport);
		                    } else {
		                        poolProvider.returnBrokenConnection(selected, transport);
		                    }
		                }
				}});
            
            
           /* ((Proxy) x).setHandler((self, thisMethod, proceed, args) -> {
                boolean success = false;
                try {
                    Object result = proceed.invoke(self, args);
                    success = true;
                    return result;
                } finally {
                    if (success) {
                        poolProvider.returnConnection(selected, transport);
                    } else {
                        poolProvider.returnBrokenConnection(selected, transport);
                    }
                }
            });*/
            return x;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("fail to create proxy.", e);
        }
    }

}
