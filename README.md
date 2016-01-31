###Nio实现细节架构图

![Nio 架构图](https://raw.githubusercontent.com/goudai/gd-rpc/master/net-work.png)

### 单机的性能测试
1.[PerfServerBootstrapTest](https://github.com/goudai/gd-rpc/blob/master/gd-rpc/src/test/java/io/goudai/rpc/performance/PerfServerBootstrapTest.java)<br />
2.[PerfClientTest](https://github.com/goudai/gd-rpc/blob/master/gd-rpc/src/test/java/io/goudai/rpc/performance/PerfClientTest.java)<br />
![QPS](https://raw.githubusercontent.com/goudai/gd-rpc/master/qps.png)


### 单机RPC调用
#### rpc server
        public class ServerBootstrapTest {
            static {
                //1 init context
                Serializer serializer = new JavaSerializer();
                Context.<Request, Response>builder()
                        .decoder(new DefaultDecoder<>(serializer))
                        .encoder(new DefaultEncoder<>(serializer))
                        .serializer(serializer)
                        .channelHandler(new RequestHandler())
                        .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                        .build()
                        .init();
            }

            public static void main(String[] args) throws Exception {
                // 2 init rpc server
                ServerBootstrap serverBootstrap = new ServerBootstrap(9999);
                //3 registry shutdown clean hook
                Runtime.getRuntime().addShutdownHook(new Thread(serverBootstrap::shutdown));
                //4 registry services..
                serverBootstrap.registry(UserService.class, new SimpleUserService());
                //5 started rpc server and await thread
                serverBootstrap.startup();


            }
        }
#### rpc client
        public class BootstrapTest {
            static {
                //1 init
                Serializer serializer = new JavaSerializer();
                Context.<Request, Response>builder()
                        .decoder(new DefaultDecoder<>(serializer))
                        .encoder(new DefaultEncoder<>(serializer))
                        .serializer(serializer)
                        .channelHandler(new ResponseHandler())
                        .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                        .build()
                        .init();
            }

            public static void main(String[] args) throws Exception {
                //2 create client
                Bootstrap bootstrap = new Bootstrap("localhost", 9999);
                //3 started client
                bootstrap.startup();
                //4 get proxy service
                UserService service = bootstrap.getService(UserService.class);
                //5 remote invoker
                User add = service.add(new User());
                // out result
                System.out.println(add);
                //7 shutdown
                bootstrap.shutdown();

            }
        }