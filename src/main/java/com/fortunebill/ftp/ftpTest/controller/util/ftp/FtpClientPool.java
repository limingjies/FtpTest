package com.fortunebill.ftp.ftpTest.controller.util.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/***
 * @Author lmj
 * @Description
 * @Date 10:49 2021/5/29
 * @Param
 * @return
**/
@Component
public class FtpClientPool{

    @Autowired
    private FtpPoolBean ftpPoolBean;

    //连接池
    private GenericObjectPool<FTPClient> ftpClientPool;

    @Autowired
    private FtpClientFactory ftpClientFactory;


    /**
     * 初始化连接池
     */
     //加上该注解表明该方法会在bean初始化后调用

    @PostConstruct
    public void init() {
        // 初始化对象池配置
        GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setBlockWhenExhausted(ftpPoolBean.isBlockWhenExhausted());
        poolConfig.setMaxWaitMillis(ftpPoolBean.getMaxWait());
        poolConfig.setMinIdle(ftpPoolBean.getMinIdle());
        poolConfig.setMaxIdle(ftpPoolBean.getMaxIdle());
        poolConfig.setMaxTotal(ftpPoolBean.getMaxTotal());
        poolConfig.setTestOnBorrow(ftpPoolBean.isTestOnBorrow());
        poolConfig.setTestOnReturn(ftpPoolBean.isTestOnReturn());
        poolConfig.setTestOnCreate(ftpPoolBean.isTestOnCreate());
        poolConfig.setTestWhileIdle(ftpPoolBean.isTestWhileIdle());
        poolConfig.setLifo(ftpPoolBean.isLifo());

        // 初始化对象池
        ftpClientPool = new GenericObjectPool<>(ftpClientFactory, poolConfig);
    }

    public FTPClient borrowObject() throws Exception {
        return ftpClientPool.borrowObject(10*1000);
    }

    public void returnObject(FTPClient ftpClient) {
        ftpClientPool.returnObject(ftpClient);
    }


}