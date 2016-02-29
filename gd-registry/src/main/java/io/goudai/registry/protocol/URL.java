package io.goudai.registry.protocol;

import io.goudai.commons.util.Assert;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 2016/2/29.
 */
@Getter
@Setter
@ToString
public class URL {
    private String service, host, port, type;

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
        url.type = split2[0].split(":")[0];
        url.host = split4[0];
        url.port = split3[0].split(":")[1];
        url.service = split3[1];



        return url;
    }

}
