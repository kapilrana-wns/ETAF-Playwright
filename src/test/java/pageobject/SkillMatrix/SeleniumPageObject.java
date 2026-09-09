package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

import java.util.List;

public class SeleniumPageObject {

	//Table
	@FindBy(how = How.CLASS_NAME, using ="table")
	public List<WebElement> table;

	//User Registration
	@FindBy(how =How.ID, using = "ConfirmPassword")
	public WebElement textConfirmPassword;
	
	@FindBy(how =How.ID, using = "AssociatedRoleID")
	public WebElement dropdownSelectRole;
	
	@FindBy(how =How.XPATH, using = "//li[contains(text(),'field is required.')]")
	public WebElement textEmptyField;

	@FindBy(how =How.XPATH, using = "//li[contains(text(),'The password and confirmation password do not matc')]")
	public WebElement textPasswordMismatch;
	
	@FindBy(how =How.XPATH, using ="//li[contains(text(),'is already taken.')]")
	public WebElement textDuplicateUser;

	// Common Control
    @FindBy(how=How.XPATH,using="//tbody/tr[11]/td[4]/a[1]")
	public WebElement lnkEditMC;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[5]/td[4]/a[1]")
	public WebElement lnkEditSC;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[5]/td[7]/a[1]")
	public WebElement lnkEditSkill;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[5]/td[6]/a[1]")
	public WebElement lnkEditSP;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[4]/td[5]/a[1]")
	public WebElement lnkEditBU;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[10]/td[4]/a[1]")
	public WebElement lnkEditGrade;		

	@FindBy(how=How.XPATH,using="//tbody/tr[7]/td[10]/a[1]")
	public WebElement lnkEditEmp;
	
	@FindBy(how=How.XPATH,using="//tbody/tr[2]/td[6]/a[1]")
	public WebElement lnkEditEF;
	
	//@FindBy(how=How.XPATH,using="//tbody/tr[1]/td[4]/a[3]")
	//public WebElement lnkDeleteVerify; 
	
	
	//Sub - menus	
	
	@FindBy(how=How.XPATH,using="//body/div[1]/div[1]/div[2]/ul[1]/li[2]/ul[1]/li[3]/a[1]")
	public WebElement subMenuSkills; 
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Evaluation Frequency')]")
	public WebElement menuEvaluationFrequency;
	
	@FindBy(how = How.LINK_TEXT, using = "BusinessUnits")
	public WebElement menuBusinessUnit;
	
	@FindBy(how=How.XPATH,using="//a[contains(text(),'Skill Proficiency')]")
	public WebElement menuSkillProficiency;
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Employee Grades')]")
	public WebElement menuEmployeesGrades;	

	@FindBy(how = How.XPATH, using = "//a[contains(text(),'User Registration')]")
	public WebElement menuUserRegistration;
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Employees')]")
	public WebElement menuEmployees;
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Assign User Role')]")
	public WebElement menuAssignUserRole;
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Skill Report')]")
	public WebElement menuSkillReport;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'Master Resource Tracker')]")
	public WebElement menuMRT;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'My Skill')]")
	public WebElement menuMySkill;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'Team Skill')]")
	public WebElement menuTeamSkill;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'Initiate Self Evaluation')]")
	public WebElement menuInitiateSelfEvaluation;

	//Evaluation Frequency
	@FindBy(how=How.ID, using = "EvaluationFrequency")
	public WebElement textEvaluationFrequency;
	
	@FindBy(how=How.ID, using = "EvalFreqStartDate")
	public WebElement textStartDate;
	
	@FindBy(how=How.ID, using = "EvalFreqEndDate")
	public WebElement textEndDate;
	

	
	//Business Unit
	@FindBy(how = How.XPATH, using = "//input[@id='BusinessEntityName']")
    public WebElement textBusinessUnitName;
	
	
	@FindBy(how = How.XPATH, using = "//input[@id='Technology']")
	public WebElement textTechnology; 
	
	@FindBy(how = How.XPATH, using = "//input[@id='BusinessHeadName']")
	public WebElement textBusinessHeadName;
	
	//Skill Proficiency
	
	@FindBy(how=How.XPATH,using="//input[@id='ProficiencyLevel']")
	public WebElement textProficiencyLevel;
	
	@FindBy(how=How.ID, using = "AssociatedOrgLevelSkillCodeID")
	public WebElement dropdownOrgProfLevel;
	
	@FindBy(how=How.ID, using = "WeightagePercent")
	public WebElement textWeightageNumber;
	
	@FindBy(how=How.ID, using ="RequireNextLevelApproval")
	public WebElement checkBoxApproval;
	

	
	//Employees Grades
		
	@FindBy(how=How.XPATH, using = "//input[@id='GradeName']")
	public WebElement textboxGradeName;
	
	//Employees
	
	@FindBy(how=How.ID, using = "EmployeeID")
	public WebElement textemplID;
	
	@FindBy(how=How.ID, using = "EmployeeName")
	public WebElement textempfullname;
	
	@FindBy(how=How.ID, using = "AssociatedGradeID")
	public WebElement dropdownGrade;
	
	@FindBy(how=How.ID, using = "AssociatedManagerID")
	public WebElement dropdownManager;
	
	@FindBy(how=How.ID, using ="AssociatedApplicationUserID")
	public WebElement dropdownUserAccount;
	
	@FindBy(how=How.ID, using ="ProjectName")
	public WebElement textProjectName;
	
	@FindBy(how=How.ID, using ="ExemptFromSkillEvaluationProcess")
	public WebElement checkBoxExemption;
	
	@FindBy(how = How.XPATH, using ="//a[contains(text(),'7')]")
	public WebElement pageNumEmp;
	

	
	//Assign user Role
	@FindBy(how = How.LINK_TEXT, using = "New User Registration")
	public WebElement lnkRegister;
	
	@FindBy(how = How.XPATH, using ="//tbody/tr[2]/td[3]/a[1]")
	public WebElement lnkUpdateRole;
	
	@FindBy(how=How.ID, using ="AssociatedRolesID")
	public WebElement selectMultipleRoles;
	
	//Reports
	
	//Skill Report and Master Resource Tracker
	
	@FindBy(how=How.XPATH, using = "//select[@id='EvaluationFrequency']")
	public WebElement dropdownofEvalFreqRecords;
	
	@FindBy(how=How.XPATH, using = "//thead/tr/th[1]")
	public WebElement reportsTable;
	
	@FindBy(how=How.XPATH, using = "//body/div[2]/form[1]/div[1]/div[2]/div[1]/input[1]")
	public WebElement btnsubmitforSkillReportandMRT;
	
	@FindBy(how=How.XPATH, using = "//body/div[2]/form[1]/div[1]/div[2]/div[1]/input[2]")
	public WebElement btnExportExcelforSkillReportandMRT;
	
	@FindBy(how=How.XPATH, using = "//h2[contains(text(),'Reports')]")
	public WebElement ReportsTitle;

	
	
	//My Skill and Team Skill
	

	@FindBy(how=How.XPATH, using = "//a[contains(text(),'Amendment Required')]")
	public WebElement btnAmendmentReq;
	
	@FindBy(how=How.LINK_TEXT, using = "Details")
	public WebElement btnDetails;
	
	@FindBy(how=How.XPATH, using = "//tbody/tr[9]/td[7]/a[1]")
	public WebElement btnDetails_Jan_Mar2010;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_11508']")
	public WebElement dropdownCurrentProf_Jan_Mar2010;
	
	//@FindBy(how=How.XPATH, using = "//tbody/tr[3]/td[7]/a[1]")
	//public WebElement btnDetails;
	
	@FindBy(how=How.XPATH, using = "//h2[contains(text(),'Employee Details')]")
	public WebElement empDetails;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[4]/select")
	public WebElement dropdownCurrentProf;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[4]")
	public WebElement dropdownCurrentProfCoEApproved;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[6]")
	public WebElement chkboxManagerApprovalRequired;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[5]")
	public WebElement chkboxPrimarySkill;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[7]")
	public WebElement dropdownDMAction;
	
	@FindBy(how=How.XPATH, using = "//h2[contains(text(),'Index')]")
	public WebElement indexPage;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[9]")
	public WebElement dropdownCoEAction;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[8]")
	public WebElement textDMNote;
	
	@FindBy(how=How.XPATH, using = "//body[1]/div[2]/main[1]/ul[2]/li[1]/table[1]/tbody/tr[2]/td[10]")
	public WebElement textCoEMgrNote;
	
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'DeliveryManagerReferredBack')]")
	public WebElement btnDMRefdBack;
	
	
	@FindBy(how=How.XPATH,using="//a[contains(text(),'COEManagerReferredBack')]")
	public WebElement btnCoERefdBack;
	
	
	@FindBy(how=How.XPATH,using="//body[1]/div[2]/table[1]/tbody[1]/tr[5]/td[1]")
	public WebElement textEvalStatus;
	

	
	//Initiate State[is]
	@FindBy(how=How.XPATH, using = "//tbody/tr[4]/td[7]/a[1]")
	public WebElement btnDetails_is;
	
	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_8156']")
	public WebElement dropdownDMAction_is;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_8156']")
	public WebElement dropdownCoEAction_is;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_8156']")
	public WebElement chkboxPrimarySkill_is;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_8156']")
	public WebElement chkboxManagerApprovalRequired_is;
	
	@FindBy(how=How.CSS, using = "#item_DMActionNote")
	public WebElement textDMNote_is;
	
	@FindBy(how=How.CSS, using = "#item_COEMgrActionNote")
	public WebElement textCoEMgrNote_is;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_8156']")
	public WebElement dropdownCurrentProf_is;
	
	//DMReview State [ dr]
	
	@FindBy(how=How.XPATH, using = "//tbody/tr[8]/td[7]/a[1]")
	public WebElement btnDetails_dr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_9010']")
	public WebElement dropdownDMAction_dr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_8945']")
	public WebElement dropdownCoEAction_dr;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_8945']")
	public WebElement chkboxPrimarySkill_dr;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_8945']")
	public WebElement chkboxManagerApprovalRequired_dr;
	
	@FindBy(how=How.CSS, using = "#DMActionNote_9010")
	public WebElement textDMNote_dr;
	
	@FindBy(how=How.CSS, using = "#item_COEMgrActionNote")
	public WebElement textCoEMgrNote_dr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_8945']")
	public WebElement dropdownCurrentProf_dr;
	
	//DM Referred Back [drb]
	@FindBy(how=How.XPATH, using = "//tbody/tr[3]/td[7]/a[1]")
	public WebElement btnDetails_drb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_7638']")
	public WebElement dropdownDMAction_drb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_7638']")
	public WebElement dropdownCoEAction_drb;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_7638']")
	public WebElement chkboxPrimarySkill_drb;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_7638']")
	public WebElement chkboxManagerApprovalRequired_drb;
	
	@FindBy(how=How.CSS, using = "#item_DMActionNote")
	public WebElement textDMNote_drb;
	
	@FindBy(how=How.CSS, using = "#item_COEMgrActionNote")
	public WebElement textCoEMgrNote_drb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_7638']")
	public WebElement dropdownCurrentProf_drb;
	
	//CoE Review State [cr]
	@FindBy(how=How.XPATH, using = "//tbody/tr[11]/td[7]/a[1]")
	public WebElement btnDetails_cr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_8765']")
	public WebElement dropdownDMAction_cr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_8765']")
	public WebElement dropdownCoEAction_cr;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_8765']")
	public WebElement chkboxPrimarySkill_cr;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_8765']")
	public WebElement chkboxManagerApprovalRequired_cr;
	
	@FindBy(how=How.CSS, using = "#item_DMActionNote")
	public WebElement textDMNote_cr;
	
	@FindBy(how=How.CSS, using = "#COEMgrActionNote_8765")
	public WebElement textCoEMgrNote_cr;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_8765']")
	public WebElement dropdownCurrentProf_cr;
	
	//CoE Referred Back State [ crb]
	
	@FindBy(how=How.XPATH, using = "//tbody/tr[11]/td[7]/a[1]")
	public WebElement btnDetails_crb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_9424']")
	public WebElement dropdownDMAction_crb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_9424']")
	public WebElement dropdownCoEAction_crb;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_9424']")
	public WebElement chkboxPrimarySkill_crb;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_9424']")
	public WebElement chkboxManagerApprovalRequired_crb;
	
	@FindBy(how=How.CSS, using = "#DMActionNote_9424")
	public WebElement textDMNote_crb;
	
	@FindBy(how=How.CSS, using = "#item_COEMgrActionNote")
	public WebElement textCoEMgrNote_crb;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_9424']")
	public WebElement dropdownCurrentProf_crb;
	
	//CoE Approved State[cas]
	
	@FindBy(how=How.XPATH, using = "//tbody/tr[2]/td[7]/a[1]")
	public WebElement btnDetails_cas;
	
	@FindBy(how=How.XPATH, using = "//label[contains(text(),'Cannot approve when COE Manger action has status a')]")
	public WebElement textErrorForCoEApproval;

	@FindBy(how=How.XPATH, using = "//select[@id='DMActions_6998']")
	public WebElement dropdownDMAction_cas;
	
	@FindBy(how=How.XPATH, using = "//select[@id='COEMgrActions_6998']")
	public WebElement dropdownCoEAction_cas;
	
	@FindBy(how=How.XPATH, using = "//input[@id='PrimarySkill_6998']")
	public WebElement chkboxPrimarySkill_cas;
	
	@FindBy(how=How.XPATH, using = "//input[@id='ManagerApprovalRequired_6998']")
	public WebElement chkboxManagerApprovalRequired_cas;
	
	@FindBy(how=How.CSS, using = "#item_DMActionNote")
	public WebElement textDMNote_cas;
	
	@FindBy(how=How.CSS, using = "#item_COEMgrActionNote")
	public WebElement textCoEMgrNote_cas;
	
	@FindBy(how=How.XPATH, using = "//select[@id='ProfStatus_6998']")
	public WebElement dropdownCurrentProf_cas;
	
	
	//Initiate Self Evaluation
	
	@FindBy(how=How.XPATH, using = "//tbody/tr[2]/td[6]/a[1]")
	public WebElement btnInitiateSelfEval;	
	
	//WorkFlow
	
	@FindBy(how=How.XPATH, using = "//table[2]/tbody[1]/tr[2]/td[5]/input[1]")
	public WebElement chkPrimary;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'Delivery Manager Review')]")
	public WebElement btnDMReview;
	
	@FindBy(how=How.XPATH, using = "//a[contains(text(),'COE Manager Review')]")
	public WebElement btnCoEReview;
	
	@FindBy(how=How.XPATH,using="//a[contains(text(),'COE Manager Approved')]")
	public WebElement btnCoEApproved;
	
	@FindBy(how=How.XPATH,using="//body/div[2]/main[1]/form[1]/div[2]/input[1]")
	public WebElement chkboxApprovalSMEandExpert;
	
	@FindBy(how=How.XPATH,using="//table/tbody/tr[2]/td[4]")
	public WebElement chkEvalStatus;
	
	//Errors
	
	@FindBy(how=How.XPATH, using = "//label[contains(text(),'Main Category already exists with same name in the selected Business Unit')]")
	public WebElement textError;
	
	@FindBy(how=How.XPATH,using="//span[contains(text(),'The Business Unit field is required.')]")
	public WebElement textBusinessUnitEmpty;
	
	@FindBy(how=How.XPATH,using="//span[contains(text(),' field is required.')]")
	public WebElement textMandatoryFieldEmpty;
	
	@FindBy(how = How.XPATH, using ="//span[contains(text(),'Given start and end date should not overlap with o')]")
	public WebElement textOverlapFreqError;
	
	@FindBy(how = How.XPATH, using ="//span[contains(text(),'End Date cannot be lesser than Start Date!!')]")
	public WebElement textErrorEndDateLesser;
	
	@FindBy(how = How.XPATH, using ="//span[contains(text(),' is not valid for ')]")
	public WebElement textErrorInvalidFormat;
	
	@FindBy(how=How.XPATH, using = "//span[contains(text(),'The total weightage of all skill proficiency canno')]")
	public WebElement textTotalWeightageError;
	
	@FindBy(how=How.XPATH, using = "//span[contains(text(),'Please provide the the details in integer. For Exa')]")
	public WebElement textWeightagePercentageError;
	
	@FindBy(how = How.XPATH, using ="//label[contains(text(),'One skill user account cannot be mapped to more th')]")
	public WebElement textErrorUserAccMapping;
	
	@FindBy(how=How.XPATH, using = "//span[contains(text(),'The EvaluationFrequency field is required.')]")
	public WebElement textErrorforReports;
	
	@FindBy(how=How.XPATH, using = "//label[contains(text(),'Skill that is marked as referred back cannot be su')]")
	public WebElement textErrorCoEReview;
	
	@FindBy(how=How.XPATH,using="//label[contains(text(),'should be selected')]")
	public WebElement textErrorMySkill;
	
	@FindBy(how=How.XPATH,using="//label[contains(text(),'you cannot mark the skill as primary')]")
	public WebElement textErrorMySkillPrimary;
	
	//a[contains(text(),'COE Manager Approved')]
	public SeleniumPageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}

}

		
	
	

