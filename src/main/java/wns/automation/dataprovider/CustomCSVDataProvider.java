package wns.automation.dataprovider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

public class CustomCSVDataProvider {

	@DataProvider(name = "CsvDataProvider")
	public Iterator getDataProvider(Method method, ITestContext context)
	{
		try
		{
		Properties props = (Properties) context.getAttribute("props");
		String testDataDir = props.getProperty("testDataDirectory");
		return getDataProvider(method.getDeclaringClass(),method, testDataDir);
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	public  Iterator getDataProvider(Class cls, Method method, String testDataDir)  
	{
		String testDataFile = testDataDir + method.getName() +".csv";
		try {
			return new CsvDataProvider(cls, method, testDataFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

//https://stackoverflow.com/questions/70080932/testng-assign-programatically-dataprovider