package pers.haike.demo.statemachine.entity;

import lombok.Data;
import pers.haike.demo.statemachine.States;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Data
@Entity
public class Cluster {
    @Id
    private String id;

    private String tryAction;
    private States tryTarget;

    @Enumerated
    private States state = States.INIT;
}
