package cn.zjiali.jd.monitor.ql;

/**
 * @author zJiaLi
 * @since 2023-03-13 11:03
 */
public interface QLUrl {

    /**
     * 获取token
     */
    String GET_AUTH_TOKEN = "/open/auth/token";

    /**
     * 更新环境变量
     */
    String ENVS = "/open/envs";

    /**
     * 运行任务
     */
    String CRONS_RUN = "/open/crons/run";

    String CRONS = "/open/crons";
}
