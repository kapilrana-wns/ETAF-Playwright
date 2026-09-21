package wns.automation.connectors.Tools;
import java.util.ArrayList;
import java.util.List;

public class Defect {

	private String defectSummary;
	private String defectDescription;
    private ArrayList<String> Tags = new ArrayList<String>();
    private String assigneeName;
    private String raisedBy;
    public String reporterName;
    
	public Defect() {}

	/** Convenience constructor */
	public Defect(String summary, String description, String reporter, String assignee, List<String> tags) {
		this.defectSummary = summary;
		this.defectDescription = description;
		this.reporterName = reporter;
		this.assigneeName = assignee;
		if (tags != null) this.Tags = new ArrayList<>(tags);
	}

	public void addTag(String tag) {
		if (this.Tags == null) this.Tags = new ArrayList<>();
		this.Tags.add(tag);
	}

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
    
	@Override
	public String toString() {
		return "Defect[summary=" + defectSummary + ", reporter=" + reporterName + ", assignee=" + assigneeName + "]";
	}

}
