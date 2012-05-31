/*
 * Protocol.java
 *
 * Created on Antradienis, 2007, Sausio 30
 *
 */

package com.pat.konungrus;

import java.util.ArrayList;

class KeyValuePair
{
	public String name = "";
	public String value = "";
	
	public KeyValuePair( String n, String v )
	{
		name = n;
		value = v;
	}
}

enum CommandDirection{ UserToBoard, BoardToServer, BoardToUser, ServerToBoard }
enum CommandAction{ Play, Take, Pass, Dump }
enum DumpTypes{ Towns, CardSet, PlayerHand, All }

class Command
{
    public CommandDirection cmdDirection = CommandDirection.UserToBoard;
    public int userId = 0;
    public int targetId = 0;
    public CommandAction action = CommandAction.Pass;
    public int[] arAddon = { -1, -1, -1, -1 , -1, -1, -1, -1, -1, -1 };
	/** What to dump */
	public DumpTypes dumpId = DumpTypes.Towns;
	public int quantity = 0;
	public int cardId;
		
	public String toString()
	{
		String strOut = "";
		String newLine = System.getProperty("line.separator");
		int pos = 0;
		
		strOut += cmdDirection.toString() + ":";
		strOut += "UserId=" + ( (userId == 0) ? "<Computer>" : userId ) + ":";
		strOut += (cardId == 0) ? "" : "CardId=" + cardId + ":";
		strOut += "TargetId=" + ( (targetId == 0) ? "<Computer>" : targetId ) + ":";
		strOut += "Action=" + action.toString() + ( (action == CommandAction.Dump) ? " " + dumpId.toString() : "" ) + ":";
		strOut += (quantity == 0) ? "" : "Quantity=" + quantity + ":";
		strOut += "Addons=";
		while( arAddon[pos] != -1 )
			strOut += arAddon[pos++] + " ";
//		strOut += newLine;
		//strOut.format("");
		
		return strOut;
	}
}

/**
 *
 * @author Aleksej V.
 */
public class Protocol
{
	
	private ArrayList m_arParams = new ArrayList();
	
	/** Creates a new instance of Protocol */
	public Protocol()
	{
	}
	
	/**
	 * Parses string and returns Command
	 * @param str containgin command 
	 * @return Command
	 */
	public Command ParseString(String str)
	{
		Command cmd = new Command();
		String[] ar = null;
		ar = str.split(":");
		String strAdd = ""; 
		int pos = 0;
		int addPos = 0;

		//System.out.println("Parsing: " + str);
		for (int i = 0; i < ar.length; i++)
		{

			if( (ar[i] != null) && (ar[i].length() > 0) )
			{
				
				if( ar[i].startsWith("UID=") )
				{
					//cmd.action = CommandAction.Take;
//				System.out.println("User ID is: " + ar[i].substring(4));
					cmd.userId = Integer.parseInt( ar[i].substring(4) );
				}
				
				if( ar[i].startsWith( "INQ=" ) )
					cmd.cmdDirection = CommandDirection.ServerToBoard;
				
				if( ar[i].startsWith("VS=") )
				{
//				System.out.println("ID of target=" + ar[i].substring(3));
					cmd.targetId = Integer.parseInt( ar[i].substring(3) );
				}
				
				if( ar[i].startsWith("END=") )
					cmd.action = CommandAction.Pass;
				
				if( ar[i].startsWith("TAKE=") )
				{
					cmd.action = CommandAction.Take;
//				System.out.println("Quantity to take is: " + ar[i].substring(5));
					cmd.quantity = Integer.parseInt( ar[i].substring(5) );
				}
				
				if( ar[i].startsWith("CARD=") )
				{
					cmd.action = CommandAction.Play;
//				System.out.println("Card to play is: " + ar[i].substring(5));
					cmd.cardId = Integer.parseInt( ar[i].substring(5) );
				}

				if( ar[i].startsWith("DUMP=") )
				{
					cmd.action = CommandAction.Dump;
//				System.out.println("Card to play is: " + ar[i].substring(5));
					pos = Integer.parseInt( ar[i].substring(5) );
					cmd.dumpId = DumpTypes.values()[pos-1];
				}

				
				if( ar[i].startsWith("ADDON=") )
				{
					pos = 6;
					addPos = 0;
					int limit = 100;
					while( pos < ar[i].length() && --limit > 0 )
					{
						strAdd = "";
						while( (pos < ar[i].length()) && (ar[i].charAt(pos) >= '0') && (ar[i].charAt(pos) <= '9') )
							strAdd += ar[i].charAt(pos++);
						
						cmd.arAddon[addPos] = Integer.parseInt( strAdd );
						while( (pos < ar[i].length()) && (ar[i].charAt(pos) == ',') )
							pos++;
						
						addPos++;
					}
				}
				
				
			}  // END IF non-empty
			
		}  // END FOR
		
		return cmd;
	}
	
	public boolean Add( String name, Object value )
	{
		KeyValuePair kvp = new KeyValuePair( name, value.toString() );
		m_arParams.add( kvp );
		return true;
	}
	
	public String toString()
	{
		String strOut = "";
		String newLine = System.getProperty("line.separator");
		KeyValuePair kvp = null;
		int total = m_arParams.size();
		
		for (int i = 0; i < total; i++)
		{
			kvp = (KeyValuePair) m_arParams.get(i);
			strOut += ( i > 0 ) ? ":" : "";
			strOut += kvp.name + "=" + kvp.value;
		}
		
		return strOut;
	}
	
}
