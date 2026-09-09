package wns.automation.core;

import com.aventstack.extentreports.ExtentTest;;

public interface IApplicationActionManager {

	default boolean Login(String url, String UserName, String Password) throws InterruptedException {
		return false;
	}

	default boolean LogOff() {
		return false;
	}
	
	default boolean LoginWithInvalidCredentials(String url, String UserName, String Password) {
		return false;
	}

//	default void InputMainCategoryData(String BusinessUnit, String MainCategoryName, String Description) {
//	}

//	default void EditInputMainCategoryData(String findtext, String BusinessUnit, String MainCategoryName, String Description) {
//	}

	default boolean createMainCategory(String BusinessUnit, String MainCategoryName, String Description) {
		return false;
	}

	default void setReportObject(ExtentTest extentTest) {
	}

	default boolean editMainCategory(String findtext, String businessUnitName, String mainCategoryName, String description) {
		return false;};
		
	default  void delete(String findtext) throws InterruptedException {
		}
	
	default  Object getPageObject() { return null; }

	default  void Search(String text) {};
	
	default  boolean createMainCategoryWithoutBU(String BusinessUnit,String MainCategoryName, String Description) {return false;}

	default  boolean createDuplicateMainCategory(String businessUnitName, String mainCategoryName, String description) {return false; }

	default  boolean  createMainCategoryWithoutMandatoryFieldInput(String businessUnit, String mainCategoryName,
			String description) {return false;}

	default boolean deleteMainCategoryErrorCase(String findtext) {return false;}

	default boolean deleteMainCategory(String findtext) {return false;}

	default boolean editMainCategoryWithDuplicates(String findtext, String businessUnitName, String mainCategoryName,
			String description) {return false;}

	default boolean editMainCategoryWithoutMandatoryFieldInput(String findtext, String businessUnit, String mainCategoryName,
			String description) {return false;}
	
	
}
