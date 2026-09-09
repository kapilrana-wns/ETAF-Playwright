package bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.File;

@CucumberOptions(
        features = "src/test/resources/features",
       glue = {"bdd.stepdefinitions", "bdd.hooks"},

        tags = "@login",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    // ✅ Parallel ready (keep false for now)
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @BeforeSuite
    public void cleanReports() {
        deleteDirectory(new File("test-output"));
        deleteDirectory(new File("reports"));

    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }
    }
}