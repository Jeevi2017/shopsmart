package com.shopsmart.dto;

public class BulkUploadResultDTO {
    private int totalProcessed;
    private int addedCount;
    private int skippedCount;
    private String message;

    public BulkUploadResultDTO() {
        super();
    }

    public BulkUploadResultDTO(int totalProcessed, int addedCount, int skippedCount, String message) {
		this.totalProcessed = totalProcessed;
		this.addedCount = addedCount;
		this.skippedCount = skippedCount;
		this.message = message;
	}

	public int getTotalProcessed() {
		return totalProcessed;
	}

	public void setTotalProcessed(int totalProcessed) {
		this.totalProcessed = totalProcessed;
	}

	public int getAddedCount() {
		return addedCount;
	}

	public void setAddedCount(int addedCount) {
		this.addedCount = addedCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

	public void setSkippedCount(int skippedCount) {
		this.skippedCount = skippedCount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
    


}
