package io.goudai.registry.protocol;

/**
 * Created by freeman on 2016/2/21.
 */

import io.goudai.commons.util.Assert;
import io.goudai.commons.util.NetUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * gdRPC
 * --$|{applicationName.version.group} 模块名称.版本号.分组 account.v1.0.0.shanghai
 * --|${service} 服务类全路径 com.goudai.test.UserService
 * --| provider 服务提供者目录
 * --|具体的服务提通知"provider://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
 * --| consumer 消费者目录
 * --|具体的调用者 "consumer://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
 */
@Getter
@Setter
@Builder
@ToString
public class Protocol {

    public static final String gdRPC = "gdRPC";
    private String application;
    private String version;
    private String group;
    private String service;
    private String host;
    private int port;
    private String type;
    private long timeout;
    private Set<String> methods = new HashSet<>();

    //将此URL装换为协议对像
    //"provider://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
    public static Protocol valueOf(String url) {
        Assert.assertNotNull("url ", url);
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Protocol protocol = Protocol.builder().build();
        String[] split = url.split("\\?");
        String[] split2 = split[0].split("//");
        String[] split3 = split2[1].split("/");
        String[] split4 = split3[0].split(":");
        //provider || consumer
        protocol.type = split2[0].split(":")[0];
        protocol.host = split4[0];
        protocol.port = Integer.valueOf(split3[0].split(":")[1]);
        protocol.service = split3[1];

        Map<String, String> map = new HashMap<>();
        String[] split1 = split[1].split("&");
        for (String s : split1) {
            String[] split5 = s.split("=");
            map.put(split5[0], split5[1]);
        }
        protocol.application = map.get("app");
        protocol.group = map.get("group");
        protocol.version = map.get("version");
        protocol.methods = new HashSet<>(Arrays.asList(map.get("methods").split(",")));
        return protocol;
    }

    public String value() {
        String s = this.methods == null ? "" : "&methods=" + this.methods.stream().collect(Collectors.joining(","));
        return this.type + "://" + this.host + ":" + this.port + "/" + this.service
                + "?timeout=" + this.timeout +
                s
                + "&app=" + this.application + "&version=" + this.version + "&group=" + this.getGroup()
                ;
    }

    @Override
    public String toString() {
        return value() + "    " + "current host " + NetUtil.getLocalIp();
    }
}
