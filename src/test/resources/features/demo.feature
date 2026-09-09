Feature: OrangeHRM Login Functionality
  Validate login behavior of OrangeHRM application

  @login @smoke
  Scenario: Validate successful login
    Given User opens OrangeHRM application
    When User enters username "Admin" and password "admin123"
    And User clicks on login button
    Then User should be navigated to dashboard page

  @login @negative
  Scenario: Validate invalid login
    Given User opens OrangeHRM application
    When User enters username "Admin" and password "wrongpass"
    And User clicks on login button
    Then Error message should be displayed