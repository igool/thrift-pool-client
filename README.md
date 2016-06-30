# thrift-pool-client
thrift-pool-client
> * raw and type safe TServiceClient pool
> * Multi backend servers support
> * Backend servers replace on the fly
> * Backend route by hash or random
> * Failover and failback support

# Get Started
mvn dependency

```xml
<dependency>
    <groupId>com.github.phantomthief</groupId>
    <artifactId>thrift-pool-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

java code sample
```java
public class ThriftMain {
	 public static void main(String[] args) throws TException {
	        // customize pool config
	        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
	        // ... customize pool config here
	        // customize transport, while if you expect pooling the connection, you should use TFrameTransport.
	        Function<ThriftServerInfo, TTransport> transportProvider = new Function<ThriftServerInfo, TTransport>() {

				@Override
				public TTransport apply(ThriftServerInfo info) {
					// TODO Auto-generated method stub
					TSocket socket = new TSocket(info.getHost(), info.getPort());
		            return socket;
				}
	            
	        };

	        Function<List<ThriftServerInfo>, List<ThriftServerInfo>> addElementFunction =
			        new Function<List<ThriftServerInfo>, List<ThriftServerInfo>>() {
			          @Override
			          public List<ThriftServerInfo> apply(List<ThriftServerInfo> list) {
			        	  return Arrays.asList(//
					                ThriftServerInfo.of("192.168.1.44", 8080)//
					                // or you can return a dynamic result.
					                );
			          }
			        };
			        
	        Supplier<List<ThriftServerInfo>> list  = Suppliers.compose(addElementFunction, new Supplier<List<ThriftServerInfo>>(){

				@Override
				public List<ThriftServerInfo> get() {
					// TODO Auto-generated method stub
					return Arrays.asList(//
			                ThriftServerInfo.of("192.168.1.44", 8080)//
			                // or you can return a dynamic result.
			                );
				}});
	      
//		 System.out.println("list1 "+list.get());
//		 System.out.println("list2 "+list.get());
		 ThriftClient thriftClient = new ThriftClientImpl( list,new DefaultThriftConnectionPoolImpl(poolConfig, transportProvider));
		 System.out.println("######start");
		 System.out.println(thriftClient.iface(ArithmeticService.Client.class).add(1,99));
		 System.out.println("######end");
	 }
}
```


# Special Thanks

perlmonk with his great team gives me a huge help. (https://github.com/PhantomThief/thrift-pool-client)
