package wns.automation.dataprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

public class ExcelDataProvider implements Iterator {

	private Sheet sheet;
	private Iterator<Row> rowIterator;
	private Class[] parameterTypes;
	private Converter[] parameterConverters;
	
	@DataProvider(name = "ExcelDataProvider")
	public  Iterator getDataProvider(Method method, ITestContext context) 
	{
		Properties props = (Properties) context.getAttribute("props");
		String testDataDir = props.getProperty("testDataDirectory");
		return getDataProvider(method.getDeclaringClass(),method, testDataDir);
	}
	
	public  Iterator getDataProvider(Class cls, Method method, String testDataDir)  
	{
		
		//String testDataFile = testDataDir + method.getName() +"xlsx";
        String testDataFile = testDataDir + method.getName() + ".xlsx";
		System.out.println("Test Data File Name is : " + testDataFile);
		try {
			return new ExcelDataProvider(cls,method,testDataFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public ExcelDataProvider(Class cls, Method method, String excelFilePath) throws IOException {
		if (method.getParameterCount() > 0) {
			System.out.println("Test Data File Name is : " + excelFilePath);
			File f = new File(excelFilePath);
			InputStream is = null;
			try {
				is = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Workbook book = null;
			try {
				book = new XSSFWorkbook(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sheet = book.getSheetAt(0); 
			rowIterator = sheet.rowIterator();
			rowIterator.next();
			parameterTypes = method.getParameterTypes();
			int len = parameterTypes.length;
			parameterConverters = new Converter[len];
			for (int i = 0; i < len; i++) {
				parameterConverters[i] = ConvertUtils.lookup(parameterTypes[i]);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return rowIterator.hasNext();

	}

	@Override
	public Object next() {
		String[] data;
		if (rowIterator != null && rowIterator.hasNext()) {
			Object[] args = parseLine(rowIterator.next());
			System.out.println(args);
			return args;

		}
		return null;
	}

	private Object[] parseLine(Row row) {
		int len = row.getPhysicalNumberOfCells();
		Object[] ovals = new Object[len];
		for (int i = 0; i < len; i++) {
			ovals[i] = parameterConverters[i].convert(parameterTypes[i], row.getCell(i));
		}
		return ovals;
	}
	
}