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
public class Group7 extends AbstractNegotiationParty {

	private Bid activeBid = null;
	private Bid maxUtilityBid = null;
	private double declineStart = 0.8;
	private double startUtility = 0.95;
	private double endUtility = utilitySpace.getReservationValueUndiscounted();
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public Group7(UtilitySpace utilitySpace, Map<DeadlineType, Object> deadlines, Timeline timeline, long randomSeed) {
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
		System.out.println(cosDiscount(scale(timeline.getTime(), declineStart, 1f, 0f, 1f), startUtility, endUtility));
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
		if (bid != null) activeBid = bid;
	}
	
	/**
	 * Generate a random bid that this agent would currently find acceptable. 
	 * @return An acceptable bid. 
	 * @throws Exception
	 */
	private Bid generateBid() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Bid bid = generateRandomBid();
			if (acceptable(bid)) return bid;
		}
		return new Bid(maxUtilityBid);
	}

	/**
	 * Determine if a bid is currently acceptable. 
	 * This depends on the negotiation progress. 
	 * Bids with a lower utility become acceptable when nearing the deadline.  
	 * @param bid
	 * @return
	 */
	private boolean acceptable(Bid bid) {
		double progress = timeline.getTime();
		return progress < declineStart ?
			getUtility(bid) > startUtility :
			getUtility(bid) > cosDiscount(scale(progress, declineStart, 1f, 0f, 1f), startUtility, endUtility);
	}
	
	/**
	 * Calculate a discount based on the cosine function. Interpolates using first quarter of the cosine.
	 * @param progress Value between 0.0 to 1.0. 
	 * @param high Return value when progress equals 0.0. 
	 * @param low Return value when progress equals 1.0. 
	 * @return Interpolated value between to and from. 
	 */
	private double cosDiscount(double progress, double high, double low) {
		return scale(Math.cos(progress*Math.PI/2), 0, 1f, low, high);
	}
	
	private double scale(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
		return toLow + (value - fromLow)/(fromHigh - fromLow)*(toHigh - toLow);
	}
}


