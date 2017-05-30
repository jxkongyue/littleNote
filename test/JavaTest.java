import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by x on 2017/5/30.
 */
public class JavaTest {

    private static class Person{

        int age;

        String name;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @Test
    public void test_assert()
    {
        String a = "hello assert";
        List<String> list = new ArrayList<String>();
        list.add(a);

        Person person = new Person();
        person.setAge(27);
        person.setName("jx");
        Assertions.assertThat(person).hasFieldOrPropertyWithValue("age",27);
    }
}
