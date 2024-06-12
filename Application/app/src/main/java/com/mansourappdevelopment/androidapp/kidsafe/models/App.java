package com.mansourappdevelopment.androidapp.kidsafe.models;
import android.os.Parcel;
import android.os.Parcelable;

public class App implements Parcelable {

	public static final Creator<App> CREATOR = new Creator<App>() {
		@Override
		public App createFromParcel(Parcel in) {
			return new App(in);
		}

		@Override
		public App[] newArray(int size) {
			return new App[size];
		}
	};
	private String appName;
	private String packageName;
	private boolean blocked;
	private int hours;
	private int minutes;
	private int seconds;
	private ScreenLock screenLock;


	public App() {
	}

	public App(String appName, String packageName, boolean blocked, int hours, int minutes, int seconds) {
		this.appName = appName;
		this.packageName = packageName;
		this.blocked = blocked;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	public App(String appName, String packageName, boolean blocked) {
		this.appName = appName;
		this.packageName = packageName;
		this.blocked = blocked;
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
	}

	protected App(Parcel in) {
		appName = in.readString();
		packageName = in.readString();
		blocked = in.readByte() != 0;
		hours = in.readInt();
		minutes = in.readInt();
		seconds = in.readInt();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean isBlocked() {
		return blocked;
	}


	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public ScreenLock getScreenLock() {
		return screenLock;
	}

	public void setScreenLock(ScreenLock screenLock) {
		this.screenLock = screenLock;
	}
	public long getTotalUsageTimeInSeconds( ) {
		int hr = hours;
		if (hr>3){
			hr = 4;
		}
		return (hr * 3600L) + (minutes * 60L) + seconds;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appName);
		dest.writeString(packageName);
		dest.writeByte((byte) (blocked ? 1 : 0));
		dest.writeInt(hours);
		dest.writeInt(minutes);
		dest.writeInt(seconds);
	}
}
