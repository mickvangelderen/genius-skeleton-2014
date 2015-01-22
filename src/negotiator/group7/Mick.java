package negotiator.group7;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class Mick extends AbstractNegotiationParty {

	private HashMap<Object, BayesianOpponentModelScalable> OpponentModels = new HashMap<Object, BayesianOpponentModelScalable>();
	private Bid activeBid = null;
	private Random random;
	private Bid maxUtilityBid = null;
	
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
		random = new Random(randomSeed);
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
			if (validActions.contains(Accept.class) && acceptable(activeBid, candidateBid)) {
				return new Accept();
			}
			return new Offer(candidateBid);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		if (bid != null) {
			// update the active bid
			activeBid = bid;
			updateOpponentModel(sender, bid);
		}
	}
	
	private Bid generateBid() throws Exception {
		// minimum bid quality for this round
		double min = scaledReservationValue();
		// start with our maximum utility bid
		Bid bestBid = new Bid(maxUtilityBid);
		double bestUtil = getAverageOpponentUtility(bestBid);
		// for 1000 random bids, check if it is good enough for us and better for the opponents 
		for (int i = 0; i < 1000; i++) {
			Bid bid = generateRandomBid();
			if (getUtility(bid) < min) continue;
			double util = getAverageOpponentUtility(bid);
			if (util > bestUtil) {
				bestBid = bid;
				bestUtil = util;
			}
		}
		return bestBid;
	}

	private boolean acceptable(Bid active, Bid candidate) {
		double currentUtility = getUtility(active);
		double candidateUtility = getUtility(candidate);
		double scaledReservation = scaledReservationValue();
		
		return currentUtility >= Math.min(candidateUtility, scaledReservation);
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

	private double scaledReservationValue() {
		double progress = timeline.getTime();
		double reservation = utilitySpace.getReservationValue();
		return scale(Math.cos(progress*Math.PI), -1f, 1f, reservation, 1f);
	}
	
	private double scale(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
		return toLow + (value - fromLow)/(fromHigh - fromLow)*(toHigh - toLow);
	}
}


