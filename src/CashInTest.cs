// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace

import org.junit.jupiter.api.*;
 import static org.junit.jupiter.api.Assertions.*;

 class CashInTest {

     @Test
     void cashInSucceedsWithValidAmount() {
         CashIn cashIn = new CashIn();
         boolean result = cashIn.cashIn(1, 100.0);
         assertTrue(result);
     }

     @Test
     void cashInFailsWithZeroAmount() {
         CashIn cashIn = new CashIn();
         boolean result = cashIn.cashIn(1, 0.0);
         assertFalse(result);
     }

     @Test
     void cashInFailsWithNegativeAmount() {
         CashIn cashIn = new CashIn();
         boolean result = cashIn.cashIn(1, -50.0);
         assertFalse(result);
     }
 }