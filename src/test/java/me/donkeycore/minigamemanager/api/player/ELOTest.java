package me.donkeycore.minigamemanager.api.player;

import static org.junit.Assert.*;

import org.junit.Test;

import me.donkeycore.minigamemanager.api.util.ELO;
import me.donkeycore.minigamemanager.api.util.ELO.GameResult;

public class ELOTest {
	
	@Test
	public void testGetUpdatedELO() {
		// Beginner
		long elo = ELO.getUpdatedELO(1000, 1000, 1, GameResult.WON);
		assertTrue(elo == 1012);
		// Pro
		elo = ELO.getUpdatedELO(2500, 1000, 30, GameResult.WON);
		assertTrue(elo == 2500);
		// Normal
		elo = ELO.getUpdatedELO(1000, 1000, 50, GameResult.WON);
		assertTrue(elo == 1007);
	}
	
}
