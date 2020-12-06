import java.util.Arrays;
import java.util.List;

public class TestStuff {

    public static void main(String[] args) {
        List<Procedure> tests = Arrays.asList(
                () -> test0(),
                () -> test1()
        );
    }

    static void test0() {
        assert true == true;
    }

    static void test1() {

    }
}

@FunctionalInterface
interface Procedure {
    void execute();
}
