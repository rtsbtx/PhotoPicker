package com.example.photopicker;

import java.util.ArrayList;

public class AlbumInfo {

	public long albumId;
	public String albumName;
	public int photoCount = 0;
	public int choiceCount = 0;
	
	public ArrayList<ItemImageInfo> imageInfos = new ArrayList<ItemImageInfo>();

}
