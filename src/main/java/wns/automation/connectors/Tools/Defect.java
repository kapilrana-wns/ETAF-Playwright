package wns.automation.connectors.Tools;
import java.util.ArrayList;

public class Defect {

	private String defectSummary;
	private String defectDescription;
    private ArrayList<String> Tags = new ArrayList<String>();
    private String assigneeName;
    private String raisedBy;
    public String reporterName;
    
	public String getReporterName() {
		return reporterName;
	}
	public void setReporterName(String reportername) {
		this.reporterName = reportername;
	}
	public String getDefectSummary() {
		return defectSummary;
	}
	public void setDefectSummary(String defectSummary) {
		this.defectSummary = defectSummary;
	}
	public String getDefectDescription() {
		return defectDescription;
	}
	public void setDefectDescription(String defectDescription) {
		this.defectDescription = defectDescription;
	}
	public ArrayList<String> getTags() {
		return Tags;
	}
	public void setTags(ArrayList<String> tags) {
		Tags = tags;
	}
	public String getAssigneeName() {
		return assigneeName;
	}
	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}
	public String getRaisedBy() {
		return raisedBy;
	}
	public void setRaisedBy(String raisedBy) {
		this.raisedBy = raisedBy;
	}
    
    
    

}
