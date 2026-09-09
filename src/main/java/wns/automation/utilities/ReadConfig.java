package wns.automation.utilities;
import java.io.FileInputStream;
import java.util.Properties;

public class ReadConfig {

	Properties properties;

	String path = System.getProperty("user.dir") + "\\src\\main\\resources\\config.properties";

	//constructor
	public ReadConfig() {
		try {
			properties = new Properties();
			FileInputStream  fis = new FileInputStream(path);
			properties.load(fis);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getValue(String str)
	{
		String value = properties.getProperty(str);

		if(value!=null)
			return value;
		else
			throw new RuntimeException("Value not specified in config file.");
	}
}