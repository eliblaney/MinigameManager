package minigamemanager.api.minigame;

import java.util.UUID;

/**
 * Outer shell for Minigame classes.
 * 
 * @author DonkeyCore
 */
public interface MinigameShell {
	
	/**
	 * Empty shell to use as a placeholder for a minigame. Contains a random UUID
	 * to distinguish it.
	 * 
	 * @author DonkeyCore
	 */
	public static final class EmptyMinigameShell implements MinigameShell {
		
		/**
		 * The random UUID that is assigned to this particular instance
		 */
		public final UUID randomID = UUID.randomUUID();
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((randomID == null) ? 0 : randomID.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EmptyMinigameShell other = (EmptyMinigameShell) obj;
			if (randomID == null) {
				if (other.randomID != null)
					return false;
			} else if (!randomID.equals(other.randomID))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "EmptyMinigameShell [randomID=" + randomID + "]";
		}
		
		/**
		 * Get the UUID that was randomly assigned to this instance
		 * 
		 * @return The random UUID
		 */
		public UUID getRandomID() {
			return randomID;
		}
		
	}
	
}
