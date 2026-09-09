package wns.automation.core;
import wns.automation.connectors.Tools.IToolsConnector;

import wns.automation.utilities.TestUtility;

import java.util.Properties;


public class BDDTestContext {

    private static IToolsConnector connector;

    public static void initialize(Properties props) {

        try {
            if (connector == null) {
                connector = TestUtility.getTestManagementToolConnector(props);
                System.out.println("BDD: Test Management Tool Initialized");
            }
        } catch (Exception e) {
            System.out.println("Error initializing connector in BDD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static IToolsConnector getConnector() {
        return connector;
    }
}
