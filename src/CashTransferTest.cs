// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace


{import org.junit.jupiter.api.*;
 import static org.junit.jupiter.api.Assertions.*;

 class CashTransferTest {

     @Test
     void transferSucceedsWithValidInput() {
         CashTransfer transfer = new CashTransfer();
         boolean result = transfer.cashTransfer(1, 2, 50.0);
         assertTrue(result);
     }

     @Test
     void transferFailsWithInsufficientBalance() {
         CashTransfer transfer = new CashTransfer();
         boolean result = transfer.cashTransfer(1, 2, 1_000_000.0);
         assertFalse(result);
     }

     @Test
     void transferFailsWithNegativeAmount() {
         CashTransfer transfer = new CashTransfer();
         boolean result = transfer.cashTransfer(1, 2, -10.0);
         assertFalse(result);
     }
 }