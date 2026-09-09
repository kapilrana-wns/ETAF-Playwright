package wns.automation.dataprovider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.opencsv.CSVReader;

public class CsvDataProvider implements Iterator {

	private CSVReader reader;
	private String[] last;
	private Class[] parameterTypes;
	private Converter[] parameterConverters;
	
	@DataProvider(name = "CsvDataProvider")
	public  Iterator getDataProvider(Method method, ITestContext context) 
	{
		Properties props = (Properties) context.getAttribute("props");
		String testDataDir = props.getProperty("testDataDirectory");
		return getDataProvider(method.getDeclaringClass(),method, testDataDir);
	}
	
	public  Iterator getDataProvider(Class cls, Method method, String testDataDir)  
	{
		
		String testDataFile = testDataDir + method.getName() +".csv";
		System.out.println("Test Data File Name is : " + testDataFile);
		try {
			return new CsvDataProvider(cls,method,testDataFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}


	public CsvDataProvider(Class cls, Method method, String csvFilePath) throws IOException {
		System.out.println("Base Directory: " + System.getProperty("user.dir"));
		if (method.getParameterCount() > 0) {
			System.out.println("Test Data File Name is : " + csvFilePath);
			InputStream is = new FileInputStream(csvFilePath);
			InputStreamReader isr = new InputStreamReader(is);
			reader = new CSVReader(isr);
			parameterTypes = method.getParameterTypes();
			int len = parameterTypes.length;
			parameterConverters = new Converter[len];
			for (int i = 0; i < len; i++) {
				parameterConverters[i] = ConvertUtils.lookup(parameterTypes[i]);
			}
		}
	}

	public boolean hasNext() {
		return (getNextLine() != null);
	}

	private String[] getNextLine() {
		if (last == null) {
			try {
				last = reader.readNext();
			} catch (IOException | CsvValidationException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return last;
	}

	public Object next() {
		String[] next;
		if (last != null) {
			next = last;
		} else {
			next = getNextLine();
		}
		last = null;
		Object[] args = parseLine(next);
		return args;
	}

	private Object[] parseLine(String[] svals) {
		int len = svals.length;
		Object[] ovals = new Object[len];
		for (int i = 0; i < len; i++) {
			ovals[i] = parameterConverters[i].convert(parameterTypes[i], svals[i]);
		}
		return ovals;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}