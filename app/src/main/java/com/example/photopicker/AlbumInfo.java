package com.example.photopicker;

import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;

public class AlbumInfo {

	public long albumId;
	public String albumName;
	public int photoCount = 0;
	public int choiceCount = 0;

    private ItemImageInfo conver;

    public void setConver(ItemImageInfo conver){
        this.conver = conver;
    }

    public ItemImageInfo getConver(){
        if(conver != null){
            return conver;
        }else{
            if(imageInfos != null && imageInfos.size() > 0){
                return imageInfos.valueAt(0);
            }else{
                return new ItemImageInfo();
            }
        }
    }
	
	private LongSparseArray<ItemImageInfo> imageInfos = new LongSparseArray<ItemImageInfo>();
    private ArrayList<Long> indexs = new ArrayList<>();

    public void addImageInfo(ItemImageInfo itemImageInfo){
        this.imageInfos.put(itemImageInfo.imageId, itemImageInfo);
        this.indexs.add(itemImageInfo.imageId);
    }

    public ItemImageInfo getImageInfoByIndex(int index){
        try {
            return this.imageInfos.get(indexs.get(index));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ItemImageInfo getImageInfoByKey(long key){
        return this.imageInfos.get(key);
    }

    public int size(){
        return this.imageInfos.size();
    }

}
