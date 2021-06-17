package com.fortunebill.ftp.ftpTest.controller.util.ftp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName FtpPoolBean
 * @Author lmj
 * @Date 2021/5/29
 * @Version 1.0
 */
@Component
@Getter
@Setter
public class FtpPoolBean {

    @Value("${ftpPool.maxTotal}")
    private int maxTotal;

    @Value("${ftpPool.minIdle}")
    private int minIdle;

    @Value("${ftpPool.maxIdle}")
    private int maxIdle;

    @Value("${ftpPool.maxWait}")
    private long maxWait;

    @Getter
    @Value("${ftpPool.blockWhenExhausted}")
    private boolean blockWhenExhausted;

    @Value("${ftpPool.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${ftpPool.testOnReturn}")
    private boolean testOnReturn;

    @Value("${ftpPool.testOnCreate}")
    private boolean testOnCreate;

    @Value("${ftpPool.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${ftpPool.lifo}")
    private boolean lifo;
}
