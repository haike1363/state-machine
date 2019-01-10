package pers.haike.demo.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class ClusterFactory extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        // interrupting action immediately when state is exited
        config.withConfiguration()
                .stateDoActionPolicy(StateDoActionPolicy.IMMEDIATE_CANCEL);
        config.withConfiguration()
                //.taskExecutor()
                .autoStartup(false);

        // interrupting action after a timeout before state is exited.
//        config.withConfiguration()
//                .stateDoActionPolicy(StateDoActionPolicy.TIMEOUT_CANCEL)
//                .stateDoActionPolicyTimeout(30, TimeUnit.SECONDS);

        // 可以指定特殊事件进入的状态
//            stateMachine.sendEvent(MessageBuilder
//                    .withPayload("E1")
//                    .setHeader(StateMachineMessageHeaders.HEADER_DO_ACTION_TIMEOUT, 5000)
//                    .build());

    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
                .withStates()
                .initial(States.INIT, context -> {
                    System.out.println("init state init");
                })
                .end(States.FINAL)
                .stateEntry(States.RUN,
                        context -> { System.out.println("state RUN entry"); },
                        context -> { System.out.println("state RUN entry error handle"); })
                .stateDo(States.RUN,
                        context -> {
                                try {
                                    System.out.println("state RUN do");
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {

                                }
                                if (Thread.interrupted()) { //必须抓到这个异常
                                    // 可能状态已经转移了
                                }
                            },
                        context -> { System.out.println("state RUN do error handle"); })
                .stateExit(States.RUN,
                        context -> { System.out.println("state RUN exit");},
                        context -> { System.out.println("state RUN exit error handle"); })

                .history(States.H_CREATING, StateConfigurer.History.SHALLOW)
                .choice(States.BAD)
                .fork(States.DELETING)
                .join(States.DELETED)
                .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal()
                    .source(States.INIT)
                    .target(States.CREATING)
                    .event(Events.START_CREATE)
                    .guard(context -> {
                        return context.getException() == null;
                    })
                    .action(context -> {
                        System.out.println("send async create");
                       // throw new RuntimeException("send async error");
                    }, context -> {
                        System.out.println("send async error");
                    }).and()
                .withHistory()//// 支持创建中，程序挂掉，继续创建
                    .source(States.H_CREATING)
                    .target(States.CREATING).and()
                .withChoice()// 根据目前的情况转换为不同的状态
                    .source(States.BAD)
                    .first(States.AUTO_RECOVER, // if
                            context -> { return false; },
                            context -> { System.out.println("do first"); },
                            context -> { System.out.println("do first error");})
                    .then(States.MANUAL_RECOVER, // elseif
                            context -> { return false; },
                            context -> { System.out.println("do then"); },
                            context -> { System.out.println("do first error");})
                    .last(States.BROKEN, // else
                            context -> { System.out.println("do last"); },
                            context -> { System.out.println("do last error");}).and()
                .withFork()
                    .source(States.DELETING)
                    .target(States.DELETING_MASTER)
                    .target(States.DELETING_SHAERSERVER).and()
                .withJoin()
                    .source(States.DELETING_MASTER)
                    .source(States.DELETING_SHAERSERVER)
                    .target(States.DELETED);
    }
}
