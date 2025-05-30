// ReSharper disable all CheckNamespace
// Please ensure all the tests are contained within the same namespace


  import org.junit.jupiter.api.*;
  import static org.junit.jupiter.api.Assertions.*;

  class UserAuthenticationTest {

      @Test
      void registerFailsWithInvalidEmail() {
          boolean result = UserAuthentication.register("Test", "bademail", "09123456789", "1234");
          assertFalse(result);
      }

      @Test
      void registerSucceedsWithValidInput() {
          String email = "user" + System.currentTimeMillis() + "@test.com";
          boolean result = UserAuthentication.register("Test User", email, "09123456789", "1234");
          assertTrue(result);
      }

      @Test
      void loginFailsWithWrongCredentials() {
          boolean result = UserAuthentication.login("notfound@test.com", "0000");
          assertFalse(result);
      }

      @Test
      void loginSucceedsWithCorrectCredentials() {
          String email = "user" + System.currentTimeMillis() + "@test.com";
          UserAuthentication.register("Test User", email, "09123456780", "1234");
          boolean result = UserAuthentication.login(email, "1234");
          assertTrue(result);
      }

      @Test
      void changePinFailsIfNotLoggedIn() {
          UserAuthentication.logout();
          boolean result = UserAuthentication.changePin("1234", "5678");
          assertFalse(result);
      }
  }