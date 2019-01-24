package pers.haike.demo.statemachine;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Data;
import org.junit.Test;

public class KryoTest {

    @Data
    static class SomeClass {
        private int i = 0;
        private String name = "s0";
        private SubClass sb;
    }

    @Data
    static class SubClass {
        public int si = 1;
        private String sname = "ss0";
        private SomeClass sc;
    }

    @Test
    public void test() {
        Kryo kryo = new Kryo();

// Register all classes to be serialized.
        kryo.register(SomeClass.class);

        SubClass subClass = new SubClass();
        subClass.setSi(2);
        subClass.setSname("22");
        SomeClass someClass = new SomeClass();
        someClass.setI(333);
        someClass.setName("333");
        someClass.setSb(subClass);
        subClass.setSc(someClass);

        Output output = new Output(1024, -1);
        kryo.writeObject(output, someClass);

        Input input = new Input(output.getBuffer(), 0, output.position());
        SomeClass object2 = kryo.readObject(input, SomeClass.class);
    }
}
