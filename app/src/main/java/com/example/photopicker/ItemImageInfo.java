package com.example.photopicker;

import java.io.Serializable;

public class ItemImageInfo implements Serializable {

	public long imageId;
	public String filePath;
	public long size;
    public String orientation;
	
	public boolean isChecked = false;

}
