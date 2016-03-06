package io.goudai.cluster.config;

import io.goudai.commons.util.Assert;
import io.goudai.commons.util.NetUtil;

/**
 * Created by Administrator on 2016/3/4.
 */
public class ClusterConfig {

    public final static String PROVIDER = "provider";
    public final static String CONSUMER = "consumer";
    public static String application = "";
    public static String version = "1.0.0";
    public static String group = "goudai";
    public static String host = NetUtil.getLocalIp();
    public static int port = 6161;
    public static int timeout = 3000;

     {
        Assert.assertNotNull("application", application);
    }


}
