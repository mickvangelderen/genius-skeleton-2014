package negotiator.group7;

import java.util.List;
import java.util.Map;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class Boulware extends AbstractNegotiationParty {

	private Bid activeBid = null;
	private Bid maxUtilityBid = null;
	private double acceptanceLevel = 0.9;
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public Boulware(UtilitySpace utilitySpace, Map<DeadlineType, Object> deadlines, Timeline timeline, long randomSeed) {
		// Make sure that this constructor calls it's parent.
		super(utilitySpace, deadlines, timeline, randomSeed);
		
		try {
			maxUtilityBid = utilitySpace.getMaxUtilityBid();
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The first party in
	 * the first round is a bit different, it can only propose an offer.
	 *
	 * @param validActions Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Action chooseAction(List<Class> validActions) {
		try {
			Bid candidateBid = generateBid();
			if (validActions.contains(Accept.class) && acceptable(activeBid))
				return new Accept();
			return new Offer(candidateBid);
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict their utility.
	 *
	 * @param sender The party that did the action.
	 * @param action The action that party did.
	 */
	@Override
	public void receiveMessage(Object sender, Action action) {
		Bid bid = Action.getBidFromAction(action);
		
		if (bid != null)
			// update the active bid
			activeBid = bid;
	}
	
	private Bid generateBid() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Bid bid = generateRandomBid();
			if (getUtility(bid) > acceptanceLevel) return bid;
		}
		return new Bid(maxUtilityBid);
	}

	private boolean acceptable(Bid active) {
		return getUtility(active) > acceptanceLevel;
	}
	
}


