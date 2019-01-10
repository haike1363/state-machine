package pers.haike.demo.statemachine;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@WithStateMachine
public class Cluster {

    @OnStateMachineStart
    void start(StateMachine<String, String> context) {

    }

    @OnStateMachineStop
    void stop(StateMachine<String, String> context) {

    }

    @OnStateMachineError
    void error(StateMachine<String, String> context, Exception e) {

    }

    // 类型安全的注解
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @OnTransition
    public @interface StatesOnTransition {
        States[] source() default {};

        States[] target() default {};
    }


    @OnTransitionStart(source = "INIT", target = "CREATING")
    public void beforeStartCreate(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @StatesOnTransition(source = States.INIT, target = States.CREATING)
    public void startCreate(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @OnTransitionEnd(source = "INIT", target = "CREATING")
    public void afterStartCreate(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @OnStateEntry(target = "CREATING")
    public void entryCreating(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @OnStateExit(source = "CREATING")
    public void exitCreating(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @OnStateChanged(source = "CREATING", target = "RUN")
    public void creatingToRun(StateContext<String, String> stateContext) {
        System.out.println(stateContext.toString());
    }

    @OnEventNotAccepted
    public void notAcceptEvent() {
        System.out.println("");
    }

    @OnExtendedStateChanged(key = "")
    public void extendedStateChange() {
    }

}
