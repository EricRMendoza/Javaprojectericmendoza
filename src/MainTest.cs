// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace


    import org.junit.jupiter.api.*;
    import static org.junit.jupiter.api.Assertions.*;

    class MainTest {

        @Test
        void mainRunsWithoutException() {
            assertDoesNotThrow(() -> Main.main(new String[]{}));
        }
    }