/*
 * Card.java
 *
 * Created on Pirmadienis, 2007, Sausio 29, 11.17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.pat.konungrus;

import java.util.ArrayList;
//import org.omg.CORBA.Environment;

//import com.sun.corba.se.spi.orbutil.fsm.Action;

/**
 *
 * @author aleksejv
 */

enum CardTypes{ Town, Building, WoodWall, BrickWall, Unit, Event, Chronicles };
enum WallTypes{ Paling, Wooden, Brick, Cremlin };

public class Card
{
	static int total = 0;
	public int cardId = 0;
	// Name of card
	public String name;
	// Description
	public String text;	// If this card is in set or is in user or out of game
	//public boolean isInSet;
	// Which type
	public CardTypes type;
	// For building it will be 0
	public int strength;
	// Only buildings will have it. Others will be 0
	public int glory;
	/** ID of user who has this card, 0 if still in set or -1 if dropped*/
	public int userId = 0;
	/** Only for Event or Chronicles. Describes target & action, i.e. - Wall:Destroy, Army:Capture */
	public String action = "";
	
	/** Creates an instance of unused Card */
	public Card()
	{
		Init();
	}
	
	public Card( int id, CardTypes t, String n)
	{
		Init( id, t, n, 0 );
	}

	public Card( CardTypes t, String n/*, String text */)
	{
		Init( 0, t, n, 0 );
	}
	
	/**
	 * If card is type of Event returns object it acts, i.e. War, Town, ...
	 * 
	 * @return
	 */
	public String ActsOn()
	{
		if( (type != CardTypes.Event) || (action == "") )
			return "";
		
		int pos = action.indexOf(':');
		if( pos == -1 )
			return action;
		
		return action.substring(0, pos);
	}
	
	public String ActsAs()
	{
		if( (type != CardTypes.Event) || (action == "") )
			return "";
		
		int pos = action.indexOf(':');
		if( pos == -1 )
			return "";
		
		return action.substring(pos+1);
	}
	
	protected void Init()
	{
		Init( 0, CardTypes.Unit, "Unused card", 0 );
	}
	
	/**
	 * Creates Card
	 * 
	 * @param id ID of Card
	 * @param t type of Card
	 * @param n name
	 * @param value glory or strength
	 */
	protected void Init( int id, CardTypes t, String n, int value )
	{
		cardId = id;
		//cardId = Card.total++;
		name = n;
		text = "";
		type = t;
		strength = 0;
		glory = 0;

		if( t == CardTypes.Building || t == CardTypes.Town )
			glory = value;
		else
			strength = value;
	}
	
	/**
	 * Returns ID of user or that it is in CardSet or dropped
	 * @return
	 */
	public String OwnerName()
	{
		switch( this.userId )
		{
			case -1:
				return "<DROPPED>";
			case 0:
				return "<CardSet>";
			default:
				return "User #" + this.userId;
		}
	}
	
	public String toString()
	{
		String strOut = "";
		String newLine =  System.getProperty("line.separator");
		
//		strOut += cardId + ". Belongs to (" + ( (userId == 0) ? "<CardSet>" : userId ) + ")" + ". ";
		strOut += cardId + ". Belongs to (" + this.OwnerName() + ")" + ". ";
		strOut += type.toString() + ": ";
		strOut += ( type == CardTypes.Building && strength > 0 ) ? "(defencive) " : "";
		strOut += name + " [" + ( (glory > 0) ? glory : strength ) + "]" + newLine;
		strOut += text;
		
		return strOut;
	}

}  // END CLASS

//class CardEvent extends Card
//{
//	/** Describes on which cards event, i.e. Building, Army, ... */
//	public String actsOn = "";
//	/** Describes action of event, i.e. Destroy, Capture */
//	public String actsAs = "";
//}  // END CLASS

class CardTown extends Card
{
	// Only <wood> or <woodWall> is possible at once
	public int woodWall = 0;
	public int paling = 0;
	
	// Only <wood> or <woodWall> is possible at once
	public int brickWall = 0;
	public int cremlin = 0;
	
	public int gloryOfBuildings = 0;
	
	/** List of buildings, only for glory! */
	private ArrayList m_arBuildings = new ArrayList();
	
	/** Creates town w/ name and glory */
	public CardTown( int id, String name, int nGlory )
	{
		Init( id, CardTypes.Town, name, nGlory );
	}
	
	// Only for non-defensive
	public boolean Build( String name, int buildGlory )
	{
//		System.out.println("Building " + name + " [" + buildGlory + "]");
		// Can't build when there are brick wall or cremlin
		if( brickWall + cremlin > 0 )
			return false;
		
		try
		{
			m_arBuildings.add( new String(name) );
		}catch ( Exception ex )
		{
			ex.printStackTrace();
			return false;
		}
		
		gloryOfBuildings += buildGlory;
		return true;
	}
	
	public boolean BuildWall( Card c/*int wStrength, CardTypes wType WallTypes wType*/ )
	{
		// Can't build when there are brick wall or cremlin
		if( brickWall + cremlin > 0 )
			return false;

		switch( c.type )
		{
			case WoodWall:
				if( woodWall > 0 )
					return false;
				name = c.name;
				woodWall = c.strength;
				break;
			case BrickWall:
				if( this.brickWall > 0 )
					return false;
				name = c.name;
				brickWall = c.strength;
				break;
//			case Paling:
//				if( woodWall > 0 )
//					return false;
//				paling = wStrength;
//				break;
//				
//			case Wooden:
//				if( paling > 0 )
//					return false;
//				woodWall = wStrength;
//				break;
//			case Brick:
//				brickWall = wStrength;
//				break;
//			case Cremlin:
//				cremlin = wStrength;
//				break;
		}  // END SWITCH
		return true;
	}
	
	public int GetWall()
	{
		return brickWall + cremlin + woodWall + paling;
	}
	
	public String toString()
	{
		String strOut = "";
		String newLine =  System.getProperty("line.separator");

		strOut += name + " #" + cardId + ". ";
		strOut += "Glory: " + glory + " + " + gloryOfBuildings + " = " + (glory + gloryOfBuildings) + " belongs to " + ( (userId == 0) ? "no one" : userId ) + ". ";
		strOut += "Defence: " + GetWall();
//		strOut += "Defence: " + ( (paling + woodWall >= 0) ? (paling + woodWall) + "(wood)" : "" );
//		strOut += ( (brickWall + cremlin > 0) ? (brickWall + cremlin) + "(brick)" : "" );
//		strOut += (brickWall + cremlin + paling + woodWall > 0) ? " = " + (brickWall + cremlin + paling + woodWall) : "";
		
		return strOut;
	}
	
}  // END CLASS


