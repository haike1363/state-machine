package pers.haike.demo.statemachine;

public enum States {
    INIT,
    CREATING,
    H_CREATING,
    RUN,
    BAD,
    AUTO_RECOVER,
    MANUAL_RECOVER,
    BROKEN,
    DELETING,
    DELETING_MASTER,
    DELETING_SHAERSERVER,
    DELETED,
    FINAL
}
