package negotiator.group7;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import agents.bayesianopponentmodel.OpponentModel;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class WeightedBidModification extends AbstractNegotiationParty {

	private HashMap<Object, Bid> lastBids = new HashMap<Object, Bid>();
	private Bid lastBid = null;
	private Random rand;
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public WeightedBidModification(UtilitySpace utilitySpace, Map<DeadlineType, Object> deadlines, Timeline timeline, long randomSeed) {
		super(utilitySpace, deadlines, timeline, randomSeed);
		rand = new Random(randomSeed);
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The first party in
	 * the first round is a bit different, it can only propose an offer.
	 *
	 * @param validActions Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class> validActions) {
		if (validActions.contains(Accept.class) && acceptable(lastBid))
			return new Accept();
		try {
			return new Offer(generateBid());
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
		lastBid = Action.getBidFromAction(action);
		lastBids.put(sender, lastBid);
	}
	
	private boolean acceptable(Bid bid) {
		return getUtilityWithDiscount(bid) > .7;
	}
	
	// Problem with this is that it doesn't try to increase utility by taking important issues and getting the value closer to where we want
	private Bid generateBid() throws Exception {
		if (rand.nextDouble() < 0.1) return generateRandomBid();
		Bid bid = getBestLastBid();
		ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();
	 	for(Issue issue : issues) {
			int id = issue.getNumber();
	 		double weight = 1 - utilitySpace.getWeight(id);
	 		if (rand.nextDouble() < weight / 10)
	 			bid.setValue(id, getRandomValue(issue));
		}
		return bid;
	}
	
	private Bid getBestLastBid() {
//		Bid best = null;
//		for (Bid bid : lastBids.values()) {
//			if (best == null || getUtility(best) < getUtility(bid))
//				best = bid;
//		}
		Bid best = lastBid;
		return best != null ? best : generateRandomBid();
	}

}
