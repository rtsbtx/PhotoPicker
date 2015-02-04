package com.example.photopicker;

public class ItemImageInfo {

	public long imageId;
	public String filePath;
	public long size;
    public String orientation; //必须是String类型，Integer不行
	
	public boolean isChecked = false;

}
