/*
 * UserHand.java
 *
 * Created on Antradienis, 2007, Sausio 30
 *
 */

package com.pat.konungrus;

import java.util.ArrayList;

/**
 *
 * @author Aleksej V.
 */
public class UserHand
{
	public int userId = 0;
	private ArrayList m_arCards = new ArrayList();
	
	/**
	 * CardSet used in current game. Could be null!
	 */
	protected CardSet m_gameCardSet = null;
	
	/**
	 * Creates a new instance of UserHand
	 * 
	 * @param gameSet CardSet used in game
	 * @param uid unique ID of user
	 */
	public UserHand( CardSet gameSet, int uid )
	{
		m_gameCardSet = gameSet;
		userId = uid;
	}

	/**
	 * Takes desired quantity of cards from set. Returns number of taken cards.
	 * Could be less if set has no cards
	 * 
	 * @param qnt
	 * @return
	 */
	public int TakeCards( int qnt )
	{
		int takenCardId = -1;
		int totalTaken = 0;
		
		while( (takenCardId != 0) && (totalTaken < qnt) )
		{
			takenCardId = m_gameCardSet.RandomId(this.userId);
			if( takenCardId > 0 )
			{
				this.AddCard(takenCardId);
				totalTaken++;
			}
		}
		
		return totalTaken;
	}
	
	/**
	 * Takes Card from game CardSet and places to UserHand. 
	 * Card is removed from game CardSet
	 * 
	 * @param cardId ID of Card to take
	 */
	private void AddCard(int cardId)/* throws Exception*/ 
	{
		Card c = m_gameCardSet.Get(cardId);
		if( c != null )
		{
			// Remove Card from game CardSet and put it to UserHand
			m_gameCardSet.ChangeOwner(cardId, this.userId);
			m_arCards.add(c);
		}else
		{
			System.out.println("User " + this.userId + " tries to add bad card ID: " + cardId);
			//throw new Exception("User " + this.userId + " tries to add bad card ID: " + cardId);
		}
	}

	public String toString()
	{
		Card card = null;
		String strOut = "";
		String newLine = System.getProperty("line.separator");
		int total = m_arCards.size();
		
		if( total > 0 )
		{
			for (int i = 0; i < total; i++)
			{
				card = (Card)m_arCards.get(i);
				strOut += card.toString() + newLine;
			}
		}else
		{
			strOut += "No cards in hand!" + newLine;
		}
		
		return strOut;
	}
	
	
	
}
