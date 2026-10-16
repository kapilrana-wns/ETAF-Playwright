package testconfig;

import org.testng.ITestContext;
import org.testng.ISuite;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.core.ITestResultManager;
import wns.automation.core.constants.Browser;
import wns.automation.core.playwright.CorePlaywrightTestManager;
import wns.automation.utilities.TestUtility;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

final class SuiteExecutionState {

    private static final String SUITE_STATE_ATTRIBUTE = SuiteExecutionState.class.getName();

    private final Properties suiteProperties;
    private final ITestResultManager resultManager;
    private final IToolsConnector testManagementConnector;
    private final CorePlaywrightTestManager suiteManager;
    private final AtomicBoolean finalized = new AtomicBoolean();

    private SuiteExecutionState(CorePlaywrightTestManager suiteManager) {
        this.suiteManager = suiteManager;
        this.suiteProperties = copyProperties(suiteManager.props);
        this.resultManager = suiteManager.getTestResultManager();
        this.testManagementConnector = suiteManager.getTestManagementToolConnector();
    }

    static SuiteExecutionState initialize(
            ISuite suite, ITestContext context, CorePlaywrightTestManager classManager) {
        synchronized (suite) {
            SuiteExecutionState state = (SuiteExecutionState) suite.getAttribute(SUITE_STATE_ATTRIBUTE);
            if (state == null) {
                classManager.TestInitialization(context);
                state = new SuiteExecutionState(classManager);
                suite.setAttribute(SUITE_STATE_ATTRIBUTE, state);
            }
            return state;
        }
    }

    Properties attachClass(
            CorePlaywrightTestManager classManager,
            String className,
            Browser selectedBrowser) {
        Properties classProperties = copyProperties(suiteProperties);
        if (selectedBrowser != null) {
            classProperties.setProperty("browser", selectedBrowser.name());
        }
        TestUtility.configureApplicationMetadata(classProperties, className);
        classManager.attachToSuite(classProperties, resultManager, testManagementConnector);
        return classProperties;
    }

    static void teardown(ISuite suite) {
        synchronized (suite) {
            SuiteExecutionState state = (SuiteExecutionState) suite.getAttribute(SUITE_STATE_ATTRIBUTE);
            if (state != null && state.finalized.compareAndSet(false, true)) {
                state.suiteManager.teardown();
            }
        }
    }

    private static Properties copyProperties(Properties source) {
        Properties copy = new Properties();
        copy.putAll(source);
        return copy;
    }
}
