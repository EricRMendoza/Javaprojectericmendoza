// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace


   import org.junit.jupiter.api.*;
   import static org.junit.jupiter.api.Assertions.*;

   class TransactionsTest {

       @Test
       void viewUserAllDoesNotThrow() {
           Transactions transactions = new Transactions();
           assertDoesNotThrow(() -> transactions.viewUserAll(1));
       }

       @Test
       void viewTransactionByIdHandlesMissingId() {
           Transactions transactions = new Transactions();
           assertDoesNotThrow(() -> transactions.viewTransactionById(-1));
       }
   }