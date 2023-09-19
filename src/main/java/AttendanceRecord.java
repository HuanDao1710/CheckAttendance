import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;

public class AttendanceRecord {
	private static final String TIME_IN = "08:30";
	private static final String TIME_OUT = "17:45";
	private String date;
	private String arrivalTime;
	private String leaveTime;
	public AttendanceRecord(String date, String arrivalTime, String leaveTime) {
		this.date = date;
		this.arrivalTime = arrivalTime;
		this.leaveTime = leaveTime;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public void setLeaveTime(String leaveTime) {
		this.leaveTime = leaveTime;
	}
	public String getDate() {
		return date;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}
	public String getLeaveTime() {
		return leaveTime;
	}
	public boolean isBeingLateLeaveEarly() {
		if(StringUtils.isBlank(leaveTime)) return false;
		return LocalTime.parse(arrivalTime).isAfter(LocalTime.parse(TIME_IN))
				|| LocalTime.parse(leaveTime).isBefore(LocalTime.parse(TIME_OUT));
	}
	public boolean hasMissedAttendance() {
		return StringUtils.isBlank(leaveTime);
	}
}
