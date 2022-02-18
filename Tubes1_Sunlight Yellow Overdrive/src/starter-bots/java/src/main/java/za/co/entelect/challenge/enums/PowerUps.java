package za.co.entelect.challenge.enums;

import com.google.gson.annotations.SerializedName;

public enum PowerUps {
    // serialized name dipake jadi nama key di GSON
    // read more: https://stackoverflow.com/questions/28957285/what-is-the-basic-purpose-of-serializedname-annotation-in-android-using-gson
    @SerializedName("BOOST") 
    BOOST,
    @SerializedName("OIL") 
    OIL,
    @SerializedName("LIZARD") 
    LIZARD,
    @SerializedName("EMP") 
    EMP,
    @SerializedName("TWEET") 
    TWEET
    // tambahin power ups di sini
}
