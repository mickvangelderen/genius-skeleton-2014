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
public class Group7 extends AbstractNegotiationParty {

	private HashMap<Object, BayesianOpponentModelScalable> opponentModels = new HashMap<Object, BayesianOpponentModelScalable>();
	private Bid activeBid = null;

	private Bid maxUtilityBid = null;
	
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
		double bestTheirs = getAverageOpponentUtility(bestBid);
		//  
		for (int i = 0; i < 5000; i++) {
			Bid bid = generateRandomBid();
			
			double ours = getUtility(bid);
			if (ours < min) continue;
			
			if (!fairEnough(bid)) continue;
			
			double theirs = getAverageOpponentUtility(bid);
			if (theirs > bestTheirs) {
				bestBid = bid;
				bestTheirs = theirs;
			}
		}
		return bestBid;
	}

	private boolean acceptable(Bid active, Bid candidate) {
		double utility = getUtility(active);
		double candidateUtility = getUtility(candidate);
		double scaledReservation = scaledReservationValue();
		
		return utility >= Math.min(candidateUtility, scaledReservation) && fairEnough(active);
	}
	
	private boolean fairEnough(Bid bid) {
		return getUtility(bid) + 0.10f > getMaximumOpponentUtility(bid); 
	}
	
	private double getMaximumOpponentUtility(Bid bid) {
		double max = Double.MIN_VALUE;
		for (BayesianOpponentModelScalable m : opponentModels.values()) {
			try {
				double u = m.getExpectedUtility(bid);
				if (u > max) max = u;
			} catch (Exception e) { e.printStackTrace(); }
		}
		return max;
	}

	private double getAverageOpponentUtility(Bid bid) throws Exception {
		Collection<BayesianOpponentModelScalable> models = opponentModels.values();
		double sum = 0;
		for (BayesianOpponentModelScalable m : models) sum += m.getExpectedUtility(bid);
		return sum/models.size();
	}
	
	private void updateOpponentModel(Object agent, Bid bid) {
		BayesianOpponentModelScalable model = opponentModels.get(agent);
		if (model == null) {
			model = new BayesianOpponentModelScalable(this.utilitySpace);
			opponentModels.put(agent, model);
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
		return scale(Math.cos(progress*Math.PI/2), 0, 1f, reservation, 1f);
	}
	
	private double scale(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
		return toLow + (value - fromLow)/(fromHigh - fromLow)*(toHigh - toLow);
	}
}


