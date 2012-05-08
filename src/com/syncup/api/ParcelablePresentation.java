package com.syncup.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePresentation implements Parcelable {

    private Presentation presentation;

    public ParcelablePresentation(Presentation point) {
         presentation = point;
    }

    public Presentation getPresentation() {
         return presentation;
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ParcelablePresentation> CREATOR
            = new Parcelable.Creator<ParcelablePresentation>() {
        public ParcelablePresentation createFromParcel(Parcel in) {
            return new ParcelablePresentation(in);
        }

        public ParcelablePresentation[] newArray(int size) {
            return new ParcelablePresentation[size];
        }
    };

    private ParcelablePresentation(Parcel in) 
    {
        presentation = new Presentation();
    	presentation.setId(in.readLong()); 
        presentation.setLoginId(in.readString());
        presentation.setName(in.readString());
        presentation.setNoSlides(in.readLong());
    }

	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeLong(presentation.getId());
		dest.writeString(presentation.getLoginId());
		dest.writeString(presentation.getName());
        dest.writeLong(presentation.getNoSlides());
	}
	
	public String toString() {
		return presentation.toString();
	}
}