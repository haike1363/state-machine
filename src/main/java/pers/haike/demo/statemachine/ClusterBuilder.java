package pers.haike.demo.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.stereotype.Component;
import pers.haike.demo.statemachine.entity.Cluster;

import java.util.EnumSet;

@Component
public class ClusterBuilder {

    @Autowired
    ClusterRepository clusterRepository;

    public StateMachine<States, Events> create(String id, Cluster cluster) throws Exception {
        cluster.setId(id);
        clusterRepository.save(cluster);
        ClusterRunner clusterRunner = new ClusterRunner();
        clusterRunner.setCluster(cluster);

        StateMachineBuilder.Builder<States, Events> builder = StateMachineBuilder.builder();
        configure(builder.configureConfiguration(), id);
        configure(builder.configureStates(), clusterRunner);
        configure(builder.configureTransitions(), clusterRunner);
        StateMachine<States, Events> stateMachine = builder.build();
        stateMachine.getExtendedState().getVariables().put("cluster", clusterRunner);
//        stateMachine.getStateMachineAccessor().withRegion().addStateMachineInterceptor(new StateMachineInterceptorAdapter<States, Events>() {
//            @Override
//            public void preStateChange(State<States, Events> state, Message<Events> message, Transition<States, Events> transition,
//                                        StateMachine<States, Events> stateMachine) {
//                // 保存状态到数据库
//                cluster.setState(state.getId());
//                clusterRepository.save(cluster);
//            }
//
//            @Override
//            public Exception stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
//                // 数据库保存异常
//                return null;
//            }
//        });
//        PersistStateMachineHandler handler = new PersistStateMachineHandler(stateMachine);
//        handler.addPersistStateChangeListener(new PersistStateMachineHandler.PersistStateChangeListener() {
//            @Override
//            public void onPersist(State<String, String> state, Message<String> message, Transition<String, String> transition, StateMachine<String, String> stateMachine) {
//
//            }
//        });
        return stateMachine;
    }

    private void configure(StateMachineConfigurationConfigurer<States, Events> config, String id) throws Exception {
        // interrupting action immediately when state is exited
        config.withConfiguration()
                .stateDoActionPolicy(StateDoActionPolicy.IMMEDIATE_CANCEL)
                //.taskExecutor()
                .autoStartup(false)
                // .beanFactory(new StaticListableBeanFactory()) 需要设置才能attach，WithStateMachine
                .machineId(id);
            // TODO 状态机的任何异常都需要触发cancel执行
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

    private void configure(StateMachineStateConfigurer<States, Events> states, ClusterRunner cluster)
            throws Exception {
        states
                .withStates()
                .initial(States.INIT,
                        context -> cluster.onInitDo(context))
                .end(States.FINAL)
                .stateEntry(States.RUN,
                        context -> cluster.entryRun(context),
                        context -> cluster.entryRunError(context))
                .stateDo(States.RUN,
                        context -> cluster.doRun(context),
                        context -> cluster.doRunError(context))
                .stateExit(States.RUN,
                        context -> cluster.exitRun(context),
                        context -> cluster.exitRunError(context))
                .history(States.H_CREATING, StateConfigurer.History.SHALLOW)
                .choice(States.BAD)
                .fork(States.DELETING)
                .join(States.DELETED)
                .states(EnumSet.allOf(States.class));
    }


    private void configure(StateMachineTransitionConfigurer<States, Events> transitions, ClusterRunner cluster)
            throws Exception {
        transitions
                .withExternal()
                    .source(States.INIT)
                    .target(States.CREATING)
                    .event(Events.START_CREATE)
                    .guard(context -> {
                        return context.getException() == null;
                    })
                    .action(context -> cluster.startCreateEventDo(context),
                            context -> cluster.startCreateEventDoError(context))
                    .and()
                .withExternal()
                    .source(States.CREATING)
                    .event(Events.CREATE_OK)
                    .target(States.RUN)
                    .action(context -> cluster.createOkEventDo(context),
                            context -> cluster.createOkEventDoError(context))
                    .and()
                .withExternal()
                    .timer(1000)
                    .source(States.RUN)
                    .target(States.BAD)
                    .guard(context -> cluster.checkBad(context))
                    .action(context -> cluster.runToBad(context))
                    .and()
                .withHistory()//// 支持创建中，程序挂掉，继续创建
                    .source(States.H_CREATING)
                    .target(States.CREATING).and()
                .withChoice()// 根据目前的情况转换为不同的状态
                    .source(States.BAD)
                    .first(States.AUTO_RECOVER, // if
                            context -> {
                                return false;
                            },
                            context -> {
                                System.out.println("do first");
                            },
                            context -> {
                                System.out.println("do first error");
                            })
                    .then(States.MANUAL_RECOVER, // elseif
                            context -> {
                                return false;
                            },
                            context -> {
                                System.out.println("do then");
                            },
                            context -> {
                                System.out.println("do first error");
                            })
                    .last(States.BROKEN, // else
                            context -> {
                                System.out.println("do last");
                            },
                            context -> {
                                System.out.println("do last error");
                            }).and()
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
