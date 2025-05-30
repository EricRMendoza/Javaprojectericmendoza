// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CheckBalanceTest {

    @Test
    void checkBalanceReturnsZeroForNewUser() {
        double balance = CheckBalance.checkBalance(9999);
        assertEquals(0.0, balance, 0.01);
    }

    @Test
    void checkBalanceReturnsCorrectAmount() {
        int userId = 1;
        CashIn cashIn = new CashIn();
        cashIn.cashIn(userId, 200.0);
        double balance = CheckBalance.checkBalance(userId);
        assertTrue(balance >= 200.0);
    }
}