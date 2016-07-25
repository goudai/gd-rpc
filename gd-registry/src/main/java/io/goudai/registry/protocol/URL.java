package io.goudai.registry.protocol;

import io.goudai.commons.util.Assert;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 2016/2/29.
 */
@Getter
@Setter
@ToString
public class URL implements Serializable {
    private String service;
    private String host;
    private String type;
    private int port;

    //"provider://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
    public static URL valueOf(String urls) {
        Assert.assertNotNull("url ", urls);
        try {
            urls = URLDecoder.decode(urls, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        URL url = new URL();
        String[] split = urls.split("\\?");
        String[] split2 = split[0].split("//");
        String[] split3 = split2[1].split("/");
        String[] split4 = split3[0].split(":");
        //provider || consumer
        String s = split2[0];
        url.type = s.split(":")[0];
        url.host = split4[0];
        url.port = Integer.valueOf(split3[0].split(":")[1]);
        url.service = split3[1];
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URL url = (URL) o;

        if (port != url.port) return false;
        if (service != null ? !service.equals(url.service) : url.service != null) return false;
        if (host != null ? !host.equals(url.host) : url.host != null) return false;
        return !(type != null ? !type.equals(url.type) : url.type != null);

    }

    @Override
    public int hashCode() {
        int result = service != null ? service.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }
}
