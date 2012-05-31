/*
 * Battle.java
 *
 * Created on February 3, 2007
 *
 */

package com.pat.konungrus;

import com.pat.konungrus.Card;

import java.util.ArrayList;

/**
 * Is we waiting for attacker or defender turn
 * @author Aleksej V.
 *
 */
enum BattleState{ No, Attack, Defend };

enum BattleStatus{ No, AttackerWins, DefenderWins };

class BattlePair
{
	public Card attackCard = null;
	public Card defendCard = null;
}

/**
 *
 * @author Aleksej V.
 */
public class Battle
{
	public BattlePair battlePair = new BattlePair();
	public BattleState battleState = BattleState.No;
	/** Id of user who attacks */
	public int attackerId = 0;
	/** Id of user who is under attack */
	public int defenderId = 0;
	// Which town is under attack. Real ID in CardSet
	public int targetId = 0;
	
	/** Contains history of battle, every card from attacker and defender will go here */
	//ArrayList m_arBattles = new ArrayList();
	
	/** Creates a dummy instance of Battle */
	public Battle( int attackerId, int defenderId, int townId)
	{
		this.attackerId = attackerId;
		this.defenderId = defenderId;
		targetId = townId;
		//battleState = BattleState.Attack;
		System.out.println("Battle is " + this.attackerId + " -> " + targetId + " which belongs to " + this.defenderId);
	}
	
	/**
	 * Returns No if still waiting for atacker or defender. In other case AttackerWins or DefenderWins
	 *
	 * @param Card card card to use
	 */
	public BattleStatus Fight( Card card )
	{
		BattleStatus status = BattleStatus.No;
		
		switch( battleState )
		{
			// Adds attacker and wait for defender
			case No:
				battlePair.attackCard = card;
				battlePair.defendCard = null;
				battleState = BattleState.Attack;
				break;
				
			// Check for strength of defender unit
			case Attack:
				battlePair.defendCard = card;
				// If militia check for town glory
				if( card.strength < 1 )
				{
					System.out.println("TODO: get town glory");
				}else
				{
					if( card.strength > battlePair.attackCard.strength )
						status = BattleStatus.DefenderWins;
					else
						status = BattleStatus.AttackerWins;
				}
				break;
		}  // END SWITCH
		
		return status;
	}
	
}  // END CLASS
