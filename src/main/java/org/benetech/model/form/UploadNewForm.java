package org.benetech.model.form;

import java.io.File;
import java.util.List;

public class UploadNewForm {
	
	List<String> officeId;
	
	File uploadFile;

	public List<String> getOfficeId() {
		return officeId;
	}

	public void setOfficeId(List<String> officeId) {
		this.officeId = officeId;
	}

	public File getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(File uploadFile) {
		this.uploadFile = uploadFile;
	}
	

}
