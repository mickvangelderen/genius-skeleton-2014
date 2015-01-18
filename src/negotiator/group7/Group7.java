package negotiator.group7;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is your negotiation party.
 */
public class Group7 extends AbstractNegotiationParty {

	private HashMap<Object, BayesianOpponentModelScalable> OpponentModels = new HashMap<Object, BayesianOpponentModelScalable>();
	private Bid lastBid = null;
	private Bid lastBidYouMade = null;
	
	
	public double utilityThreshold = 0.75;
	
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public Group7(UtilitySpace utilitySpace,
				  Map<DeadlineType, Object> deadlines,
				  Timeline timeline,
				  long randomSeed) {
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
	@Override
	public Action chooseAction(List<Class> validActions) {
		Bid myBid = generateBid();
		
		
		if (validActions.contains(Accept.class) && shouldAccept(myBid)) {
			lastBidYouMade = lastBid;
			return new Accept();
		}
		lastBidYouMade = myBid;
		return new Offer(myBid);
	}

	private Bid generateBid() {
		
		Bid bid = null;
		if (lastBidYouMade == null)  // It's your first bid
		{
			try {
				bid = utilitySpace.getMaxUtilityBid();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			
		}
		else  // Not your first bid
		{	
			
			double target = getUtility(lastBidYouMade);
			
			try {

				System.out.print("Agent " + this.getPartyId() + "in round " + this.getTimeLine().getCurrentTime() +
						" thinks the utility of his last bid was " + target + " for himself");
				
				int i = 1;
				for (BayesianOpponentModelScalable model : OpponentModels.values())
				{
					System.out.print(", " + model.getExpectedUtility(lastBidYouMade) + " for Opponent" + i);
					i++;
				}
				
				System.out.println(", " + getAverageOpponentUtility(lastBidYouMade) + " on average.");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			bid = generateBidBetterThan (target);

			for (int i = 1; i <= 5 && bid == null; i++)
				bid = generateBidBetterThan (target - i*0.05);
			
			
		}
		
		
		if (bid == null)
			System.out.println("Agent " + this.getPartyId() + " is returning null bid");

		return bid;
	}
	
	private Bid generateBidBetterThan (double target)	{
		Bid bid = null;
		int loops = 0;
		try {
			do {
				bid = this.generateRandomBid();
				loops++;
				
				if(getUtility(bid) >= target && getAverageOpponentUtility(bid) > getAverageOpponentUtility(lastBidYouMade))
					return bid;
				else if (loops > 100000)
					return null;
			} while (true);
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
		
	
	private boolean shouldAccept(Bid candidateBid) {
		
		boolean cConst = getUtility(lastBid) >= utilityThreshold;
		
		boolean cNext = getUtility(lastBid) >= getUtility(candidateBid);
		
		boolean cTime = this.getTimeLine().getCurrentTime() > this.getTimeLine().getTotalTime()/2;
		
		System.out.println(">> I am agent " + this.getPartyId() + "in round " + this.getTimeLine().getCurrentTime() +
				". Utility of my candidate bid: " + getUtility(candidateBid) + ". Utility of last bid received: " + getUtility(lastBid) + ". I should accept: " + cNext + "\n");

		return cNext || (cConst && cTime) ;
		//return cNext;
	}
	
	private double getAverageOpponentUtility(Bid bid) {
		Collection<BayesianOpponentModelScalable> models = OpponentModels.values();
		double sum = 0;
		try {
			for (BayesianOpponentModelScalable m : models) {
			    sum += m.getExpectedUtility(bid);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return sum/models.size();
	
	}
	
	private void updateOpponentModel(Object agent, Bid bid) {
		BayesianOpponentModelScalable model = OpponentModels.get(agent);
		if (model == null)
		{
			model = new BayesianOpponentModelScalable(this.utilitySpace);
			OpponentModels.put(agent, model);
		}
		try {
			model.updateBeliefs(bid);
		} catch (Exception e) {
			//e.printStackTrace();
		}
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
		if (action instanceof Offer) {
			lastBid = Action.getBidFromAction(action);
		}
		
		if (lastBid == null)
			System.out.println("lastBid is null");
		
		updateOpponentModel(sender, lastBid);
		
	}


}


