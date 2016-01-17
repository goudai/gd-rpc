package io.goudai.commons.pool;

/**
 * Created by freeman on 2016/1/17.
 */
public interface KVPool<K,V> {
    /**
     * 通过传入K返回池中的vaule对象
     * @param k
     * @return
     * @throws Exception
     */
     V borrowObject(K k) throws Exception;
    /**
     * 将使用完毕的KV放回池子中
     * @param k
     * @return
     * @throws Exception
     */
    void returnObject(K k,V v) throws Exception;


}
