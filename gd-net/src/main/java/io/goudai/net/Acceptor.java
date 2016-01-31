package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Created by freeman 2016/1/8.
 * 用于accept新连接
 */
@Slf4j
public class Acceptor extends Thread implements Lifecycle {
    /*处理accept的选择器*/
    private final Selector selector;
    /*
    * 核心读写处理池
    * 获取新的accept链接之后会从该pool中获取到一个reactor读写事件选择器
    * 并注册到该selecrot
    * */
    private final ReactorPool reactorPool;
    private final ServerSocketChannel serverSocketChannel;

    public Acceptor(String name, InetSocketAddress bindSocketAddress, ReactorPool reactorPool) throws IOException {
        super(name);
        this.setDaemon(true);
        this.selector = Selector.open();
        this.reactorPool = reactorPool;
        this.serverSocketChannel = (ServerSocketChannel) ServerSocketChannel.open().bind(bindSocketAddress).configureBlocking(false);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 16 * 2);
        this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        log.info("started server,bing socket address ={},port={}", bindSocketAddress.getHostName(), bindSocketAddress.getPort());
    }

    @Override
    public void startup() {
        this.start();
        log.info("accept {} started success",this.getName());
    }

    @Override
    public void shutdown() {
        log.info("accept {} shutdowning",this.getName());
        try {
            this.selector.close();
            this.serverSocketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        while (!interrupted()) {
            final Selector selector = this.selector;
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                try {
                    selectionKeys.forEach(this::accept);
                } finally {
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * 处理accpect事件
     * @param key
     */
    private void accept(SelectionKey key) {
            try {
                if (key.isValid() && key.isAcceptable())
                reactorPool.register((SocketChannel) ((ServerSocketChannel) key.channel()).accept().configureBlocking(false));
                else key.cancel();
            } catch (IOException e) {
               log.error(e.getMessage(),e);
            }

    }


}
