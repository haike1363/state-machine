package pers.haike.demo.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;
import pers.haike.demo.statemachine.entity.Cluster;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SsmApplicationTests {

    @Autowired
    StateMachineFactory<States, Events> factory;

    @Autowired
    ClusterBuilder clusterBuilder;

    @Test
    public void testFactory() {
        for (int i = 0; i < 3; ++i) {
            StateMachine<States, Events> m = factory.getStateMachine("f" + i);
            m.getExtendedState().getVariables().put("cluster", new Object());
            log.info(m.toString());
        }
    }

    @Test
    public void testBuilder() throws Exception {
        for (int i = 0; i < 3; i++) {
            StateMachine<States, Events> m = clusterBuilder.create("b" + i, new Cluster());
            log.info(m.toString());
        }
    }

    @Test
    public void testCreateEvent() throws Exception {
        StateMachine<States, Events> machinef = clusterBuilder.create("test", new Cluster());
        machinef.start();
        machinef.sendEvent(Events.START_CREATE);
        // machinef.sendEvent(Events.CREATE_OK);
        // 事件是异步顺序处理的，如果没有被异步处理，会默默丢弃
        // machinef.sendEvent(Events.START_DELETE);
        Thread.sleep(10 * 1000);
    }
}
