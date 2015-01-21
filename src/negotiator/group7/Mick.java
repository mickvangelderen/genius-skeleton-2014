package negotiator.group7;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
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
public class Mick extends AbstractNegotiationParty {

	private HashMap<Object, BayesianOpponentModelScalable> OpponentModels = new HashMap<Object, BayesianOpponentModelScalable>();
	private Bid activeBid = null;
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public Mick(UtilitySpace utilitySpace, Map<DeadlineType, Object> deadlines, Timeline timeline, long randomSeed) {
		// Make sure that this constructor calls it's parent.
		super(utilitySpace, deadlines, timeline, randomSeed);
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
		Bid candidateBid = generateBid();
		if (validActions.contains(Accept.class) && shouldAccept(activeBid, candidateBid)) {
			return new Accept();
		}
		return new Offer(candidateBid);
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
		
		if (bid != null) {
			// update the active bid
			activeBid = bid;
			updateOpponentModel(sender, bid);
		}
	}
	
	private Bid generateBid() {
		try {
			// Check if we're the first
			if (activeBid == null) return utilitySpace.getMaxUtilityBid();
			
			// Compute random bids while keeping track of the best one and lowering the required utility
			Bid maxBid = generateRandomBid();
			double maxOurUtil = getUtility(maxBid);
			double maxTheirUtil = getAverageOpponentUtility(maxBid);
			
			double minUtility = utilitySpace.getReservationValue();
			double step = (1f - minUtility)/1000;
			for (double utility = 1f; utility > minUtility; utility -= step) {
				Bid bid = generateRandomBid();
				double ourUtil = getUtility(bid);
				double theirUtil = getAverageOpponentUtility(bid);
				if (ourUtil > maxOurUtil && theirUtil > maxTheirUtil) {
					maxOurUtil = ourUtil;
					maxTheirUtil = theirUtil;
					maxBid = bid;
				}
				if (maxOurUtil >= utility && maxTheirUtil >= utility) return maxBid;
			}
			
			// If none found, be stubborn and return your max util bid
			return utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// If none found, be stubborn and return your max util bid
		return null;
	}

	private boolean shouldAccept(Bid active, Bid candidate) {
		double currentUtility = getUtility(active);
		double candidateUtility = getUtility(candidate);
		double progress = timeline.getTime();
		double reservation = utilitySpace.getReservationValue();
		
		return currentUtility >= candidateUtility ||
				currentUtility >= interpolate(1f, reservation, progress*10/9); 
	}
	
	private double getAverageOpponentUtility(Bid bid) throws Exception {
		Collection<BayesianOpponentModelScalable> models = OpponentModels.values();
		double sum = 0;
		for (BayesianOpponentModelScalable m : models) sum += m.getExpectedUtility(bid);
		return sum/models.size();
	}
	
	private void updateOpponentModel(Object agent, Bid bid) {
		BayesianOpponentModelScalable model = OpponentModels.get(agent);
		if (model == null) {
			model = new BayesianOpponentModelScalable(this.utilitySpace);
			OpponentModels.put(agent, model);
		}
		try {
			model.updateBeliefs(bid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double interpolate(double from, double to, double progress) {
		return from + (to - from)*between(progress, 0f, 1f);
	}
	
	private double between(double value, double min, double max) {
		return Math.max(min, Math.min(value, max));
	}
}


