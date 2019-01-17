package pers.haike.demo.statemachine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import pers.haike.demo.statemachine.entity.Cluster;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

@Data
@Slf4j
public class ClusterRunner {

    private Cluster cluster;

    @Autowired
    private ClusterRepository clusterRepository;

    private Random random = new Random(new Date().getTime());

    void doWork(String val, int seconds) {
        log.info("do work ==> {} for {} seconds", val, seconds);
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {

        }
    }

    void doWorkMayError(String val, int seconds) {
        log.info("do work {} for {} seconds", val, seconds);
        try {
            Thread.sleep(random.nextInt(seconds * 1000));
        } catch (Exception e) {
            log.info("do {} for {} seconds error", val, seconds);
        }

        if (random.nextBoolean()) {
           // throw  new RuntimeException("do work " + val + " error");
        }
    }

    void doWorkError(String val, int seconds) {
        log.info("do work {} for {} seconds error", val, seconds);
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
        }
        throw  new RuntimeException("do work "+ val + " error");
    }

    String stateContextToString(StateContext<States, Events> stateContext) {
        StringBuilder builder = new StringBuilder();
        builder.append("----->>\n");
        builder.append("state " + stateContext.getStateMachine().getState()).append("\n");
        builder.append("source " + stateContext.getSource()).append("\n");
        builder.append("target " + stateContext.getTarget()).append("\n");
        if (stateContext.getSources() != null) {
            builder.append("sources " + Arrays.toString(stateContext.getSources().toArray())).append("\n");
        }
        if (stateContext.getTargets() != null) {
            builder.append("targets " + Arrays.toString(stateContext.getTargets().toArray())).append("\n");
        }
        builder.append("event " + stateContext.getEvent()).append("\n");
        builder.append("transtion source" + stateContext.getTransition().getSource()).append("\n");
        builder.append("transtion target" + stateContext.getTransition().getTarget()).append("\n");
        builder.append("-----<<\n");
        return builder.toString();
    }

    void onInitDo(StateContext<States, Events> stateContext) {
        log.info("\nonInitDo\n{}", stateContextToString(stateContext));
        doWork("onInitDo", 1);
    }

    void entryRun(StateContext<States, Events> stateContext) {
        log.info("\nentryRun\n{}", stateContextToString(stateContext));
        doWorkMayError("entryRun", 1);
    }

    void entryRunError(StateContext<States, Events> stateContext) {
        log.info("\nentryRunError\n{}", stateContextToString(stateContext));
        log.info("Exception: {}", stateContext.getException().getMessage());
    }

    void doRun(StateContext<States, Events> stateContext) {
        log.info("\ndoRun\n{}", stateContextToString(stateContext));
        doWorkMayError("doRun", 1);
        if (Thread.interrupted()) { //必须抓到这个异常
            // 可能状态已经转移了
        }
    }

    void doRunError(StateContext<States, Events> stateContext) {
        log.info("\ndoRunError\n{}", stateContextToString(stateContext));
        log.info("\nException: {}", stateContext.getException().getMessage());
    }

    void exitRun(StateContext<States, Events> stateContext) {
        log.info("\nexitRun\n{}", stateContextToString(stateContext));
        doWorkMayError("exitRun", 1);
    }

    void exitRunError(StateContext<States, Events> stateContext) {
        log.info("\nexitRunError\n{}", stateContextToString(stateContext));
        log.info("Exception: {}", stateContext.getException().getMessage());
    }

    void createOkEventDo(StateContext<States, Events> stateContext) {
        log.info("\ncreateOkEventDo\n{}", stateContextToString(stateContext));
        doWorkMayError("createOkEventDo", 1);
    }

    void createOkEventDoError(StateContext<States, Events> stateContext) {
        log.info("\ncreateOkEventDoError\n{}", stateContextToString(stateContext));
        log.info("Exception: {}", stateContext.getException().getMessage());
    }

    boolean checkBad(StateContext<States, Events> stateContext) {
        boolean ret = random.nextBoolean();
        log.info("\ncheckBad {}", ret);
        return ret;
    }

    void runToBad(StateContext<States, Events> stateContext) {
        log.info("\nrunToBad\n{}", stateContextToString(stateContext));
        doWork("runToBad", 1);
    }

    // 失败后状态不会转换
    public void startCreateEventDo(StateContext<States, Events> stateContext) {
        // prepare action
        cluster.setTryAction(stateContext.getEvent().name());
        cluster.setTryTarget(stateContext.getTransition().getTarget().getId());
        clusterRepository.save(cluster);
        log.info("\nstartCreateEventDo\n{}", stateContextToString(stateContext));
        doWorkMayError("startCreateEventDo", 1);
        // commit
        cluster.setTryAction("");
        cluster.setTryTarget(null);
        cluster.setState(stateContext.getTarget().getId());
        clusterRepository.save(cluster);
    }

    // cancel状态机
    public void startCreateEventDoError(StateContext<States, Events> stateContext) {
        // cancel action
        // if database has nvs info delete it
        // else nvs info not save manual must to recover it
        log.info("\nstartCreateEventDoError\n{}", stateContextToString(stateContext));
        log.info("Exception: {}", stateContext.getException().getMessage());

        cluster.setTryTarget(null);
        cluster.setTryAction("");
        clusterRepository.save(cluster);
    }


    // 任何异常状态都需要执行他进行回滚恢复
    public void rollback() {

    }
}
