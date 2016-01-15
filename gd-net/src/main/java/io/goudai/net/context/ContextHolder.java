package io.goudai.net.context;

/**
 * Created by Administrator on 2016/1/14.
 */
public class ContextHolder {
    static Context context;

    public static Context<Object, Object> getContext() {
        return ContextHolder.context;
    }

    public static void registed(Context context) {
        ContextHolder.context = context;
    }
}
