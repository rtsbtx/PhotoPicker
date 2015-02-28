package com.example.photopicker;

import android.support.v4.util.LongSparseArray;

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

    public void addImageInfo(ItemImageInfo itemImageInfo){
        this.imageInfos.put(itemImageInfo.imageId, itemImageInfo);
    }

    public ItemImageInfo getImageInfoByIndex(int index){
        return this.imageInfos.valueAt(index);
    }

    public ItemImageInfo getImageInfoByKey(long key){
        return this.imageInfos.get(key);
    }

    public int size(){
        return this.imageInfos.size();
    }

}
