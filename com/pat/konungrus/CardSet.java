/*
 * CardSet.java
 *
 * Created on Pirmadienis, 2007, Sausio 29, 11.48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.pat.konungrus;

import com.pat.konungrus.CardTypes;
import java.util.Random;

/**
 * Provides interface to manage CardSet - Get cards, Compare
 * @author Aleksej V.
 */
public class CardSet
{
	/**
	 *  How many cards are in set
	 */
	private int m_availableCards = 0;
	public int totalCards = 0;
	public int totalTowns = 0;

	Random m_cRand = new Random();

	private int m_citiesQnt = 11;
	private Card[] m_arCards = new Card[128];
	
	/**
	 * Creates a new instance of CardSet
	 */
	public CardSet()
	{
		Init();
	}
	
	public boolean Init()
	{
		String arCities[] = {"Kiev", "Chernigov", "Novgorod", "Galich", "Pskov", "Tmutarakan", "Murom"};
		String arMercenary[] = {"Vikings_7 ", "Hungary_6", "Poland_5 ", "Polovcy_4 ", "Polovcy_3", "Polovcy_2", "Polovcy_1" };
		String arEvents[] = {"Metropolitan condemnation", "Viking To Greek", "Mitropolitan Justice"};

		totalCards = 0;
		int cardId = 1;
		
		totalTowns = arCities.length;
		for (int i = 0; i < arCities.length; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.Town, arCities[i] );
			m_arCards[totalCards].glory = ( i < 2 ) ? 3 : ( i > 4 ) ? 1 : 2;
			totalCards++;
		}
		
		for (int i = 1; i < 11; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.Unit, "Druzhina " + i );
			m_arCards[totalCards].strength = i;
			totalCards++;
		}
		
		for (int i = 0; i < arMercenary.length; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.Unit, arMercenary[i] );
			m_arCards[totalCards].strength = arMercenary.length - i;
			totalCards++;
		}
		
		for (int i = 0; i < 10; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.Building, "Church" );
			m_arCards[totalCards].glory = 1;
			totalCards++;
		}

		for (int i = 0; i < 3; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.Building, "Cathedral" );
			m_arCards[totalCards].glory = 3;
			totalCards++;
		}
		
		for (int i = 0; i < 3; i++)
		{
			m_arCards[totalCards] = new Card( cardId++, CardTypes.WoodWall, "Wooden wall" );
			m_arCards[totalCards].strength = 3;
			totalCards++;
		}
		
		m_arCards[totalCards] = new Card( cardId++, CardTypes.Event, arEvents[0] );
		m_arCards[totalCards].action = "War:Stop";
		totalCards++;

		m_arCards[totalCards] = new Card( cardId++, CardTypes.Event, arEvents[1] );
		m_arCards[totalCards].action = "CardSet:3";
		totalCards++;
		
		m_arCards[totalCards] = new Card( cardId++, CardTypes.Event, arEvents[2] );
		m_arCards[totalCards].action = "Town:Return";
		totalCards++;


		m_availableCards = totalCards;

		return true;
	}
	
	/**
	 * Returns ID of random card AVAILABLE in set. If no card is in set returns 0
	 * 
	 * @param userId ID of user to take
	 * @return
	 */
	public int RandomId( int userId )
	{
		System.out.println("There are " + m_availableCards + " cards left in set");
		if( m_availableCards < 1 )
		{
			return 0;
		}

		int randId = 0;
		
		do
		{
			randId = m_cRand.nextInt(totalCards) + 1;
		}while( m_arCards[randId - 1].userId != 0 );
		
		m_arCards[randId - 1].userId = userId;
		m_availableCards--;
		return randId;
	}
	
	/**
	 * Returns Card by its ID in set or null if incorrect ID
	 * Do not checks permissions!
	 * 
	 * @param id
	 * @return
	 */
	public Card Get( int id )
	{
		return ( id < 1 || id > totalCards ) ? null : m_arCards[id - 1];
	}
	
	/**
	 * Returns all Cards even out of current game
	 */
	public String toString()
	{
		// Return all cards, even out of game
		return toString(false);
	}
	
	/**
	 * Returns list of Cards
	 * 
	 * @param onlyActive whether to return active or all Cards
	 * @return list of Cards
	 */
	public String toString( boolean onlyActive )
	{
		String strOut = "";
		String newLine =  System.getProperty("line.separator");
		int total = 0;

		if( onlyActive )
		{
			for (int i = 0; i < totalCards; i++)
			{
				// Display if this card belongs to no one
				if( m_arCards[i].userId == 0 )
				{
					strOut += m_arCards[i].toString() + newLine;
					total++;
				}
			}
		}
		else
		{
			total = m_arCards.length;
			try
			{
				for (int i = 0; i < totalCards; i++)
				{
					strOut += m_arCards[i].toString() + newLine;
				}
			}
			catch ( Exception e )
			{
				strOut += "error" + newLine;
			}
		}
		
		if( total < 1 )
			strOut = "No cards in set!";
		else
			strOut = "There are " + /*m_arCards.length*/ totalCards + " cards in set" + newLine + strOut;

		return strOut;
	}

	/**
	 * Set ownership of specified Card
	 * 
	 * @param cardId ID of Card to own
	 * @param userId ID of user
	 */
	public void ChangeOwner(int cardId, int userId)
	{
		if( userId == -1 )
		{
			System.out.println("Card #" + cardId + " is dropped...");
		}else if( userId == 0 )
		{
			System.out.println("Card #" + cardId + " is returned to CardSet...");
		}else
		{
			System.out.println("Card #" + cardId + " goes to user #" + userId);
		}

		Card c = this.Get(cardId);
		if( c != null )
		{
			c.userId = userId;
		}
	}
	
	/**
	 * Removes Card from game CardSet
	 * 
	 * @param cardId
	 */
	public void Remove(int cardId)
	{
		// Change ownership to DROPPED
		this.ChangeOwner(cardId, -1);
	}
	
}
