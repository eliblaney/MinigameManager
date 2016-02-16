package me.donkeycore.minigamemanager.api.winner;

import java.util.UUID;

public final class MultipleWinnerList implements WinnerList {
	
	private UUID[] firstPlace, secondPlace, thirdPlace;
	private String fName, sName, tName;
	
	public MultipleWinnerList(UUID[] firstPlace) {
		this.firstPlace = firstPlace;
	}
	
	public MultipleWinnerList(UUID[] firstPlace, UUID[] secondPlace) {
		this.firstPlace = firstPlace;
		this.secondPlace = secondPlace;
	}
	
	public MultipleWinnerList(UUID[] firstPlace, UUID[] secondPlace, UUID[] thirdPlace) {
		this.firstPlace = firstPlace;
		this.secondPlace = secondPlace;
		this.thirdPlace = thirdPlace;
	}
	
	public UUID[] getFirstPlace() {
		return firstPlace;
	}
	
	public void setFirstPlace(UUID[] firstPlace) {
		this.firstPlace = firstPlace;
	}

	@Override
	public String getFirstPlaceName() {
		return fName;
	}

	@Override
	public void setFirstPlaceName(String name) {
		this.fName = name;
	}
	
	public UUID[] getSecondPlace() {
		return secondPlace;
	}
	
	public void setSecondPlace(UUID[] secondPlace) {
		this.secondPlace = secondPlace;
	}

	@Override
	public String getSecondPlaceName() {
		return sName;
	}

	@Override
	public void setSecondPlaceName(String name) {
		this.sName = name;
	}
	
	public UUID[] getThirdPlace() {
		return thirdPlace;
	}
	
	public void setThirdPlace(UUID[] thirdPlace) {
		this.thirdPlace = thirdPlace;
	}

	@Override
	public String getThirdPlaceName() {
		return tName;
	}

	@Override
	public void setThirdPlaceName(String name) {
		this.tName = name;
	}
	
}
