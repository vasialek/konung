/*
 * Server.java
 *
 * Created on January 30, 2007, 6:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.pat.konungrus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import com.pat.konungrus.Battle;

enum ServerState{ Stopped, Waiting, Game, Battle, BattleOver, CountDown };

/**
 *
 * @author Lekha
 */
public class Server
{
	CardSet m_cardSet = new CardSet();
	ServerSocket m_socket = null;
	Socket[] m_arSockets = null;
	Protocol m_cProtocol = new Protocol();
	Battle m_cBattle = null;

	/**  */
	BufferedReader[] m_arR = null;
	PrintWriter[] m_arW = null;

	
	/** Buildings and chronicles on the board */
	int m_nBuildings = 0;
	CardTown[] m_arBuildings = null;
	
	/** Contains list of users this town belong to. Used to return */
	private ArrayList m_arOwners = new ArrayList();
	
	int m_totalUsers = 0;
	int m_port = 9791;
	ServerState m_state = ServerState.Stopped;
	/** Will be set after Tatary or last card is taken from set. Server will be in CountDown state */
	private int m_turnsLeft = 0;
	/** Id of user to accept command */
	int m_userToAccept = 0;
	/** Id of user who is attacked by current user */
	int m_userUnderAttack = 0;

	private String m_nl = System.getProperty("line.separator");
	
	/** Creates a new instance of Server */
	public Server()
	{
		try
		{
			m_socket = new ServerSocket(m_port);
			m_socket.setSoTimeout(30 * 1000);
			m_arBuildings = new CardTown[m_cardSet.totalTowns];
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/** Creates new Readers and Writers for each user */
	public void InitUserCommunications( int totalUsers )
	{
		m_totalUsers = totalUsers;
		try
		{
			m_arR = new BufferedReader[m_totalUsers];
			m_arW = new PrintWriter[m_totalUsers];

			m_arSockets = new Socket[totalUsers];
			
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public boolean Run()
	{
		Command cmd = null;
		String strCmd = "";
		String msg = "";
		boolean isOk = true;
		int limit = 10;
		
		//System.out.println(m_cardSet.toString());
		
		if( !WaitForUsers() )
		{
			System.out.println("Error waiting for users!");
			isOk = false;
		}
		
		m_state = ServerState.Game;
		try
		{
			for (int i = 0; i < m_arSockets.length; i++)
			{
				m_arR[i] = new BufferedReader( new InputStreamReader(m_arSockets[i].getInputStream() ) );
				m_arW[i] = new PrintWriter( m_arSockets[i].getOutputStream(), true );
				m_arW[i].println("Your turn is #" + (i+1));
				
			}  // END FOR
		}catch(IOException ex)
		{
			ex.printStackTrace();
			isOk = false;
		}
		
		
		while( isOk )
		{
			try
			{
				for(int i = 0; i < m_arSockets.length; i++)
				{
					// Parse only his command
					m_userToAccept = i + 1;
					// No war
					m_userUnderAttack = 0;
					
					/** In Battle state we accept commands only from attacker & defender */
					switch( m_state )
					{
//						case Battle:
//							System.out.println("Waiting for attacker #" + m_cBattle.attackerId + " or defender #" + m_cBattle.defenderId);
//							if( (m_cBattle.attackerId-1 == i) || (m_cBattle.defenderId-1 == i) )
//							{
//								m_arW[i].print("Your turn>" + m_nl);
//								m_arW[i].flush();
//								strCmd = m_arR[i].readLine();
//							}else
//								strCmd = "";
//							break;
						default:
							if( (i+1) == m_userToAccept)
							{
								m_arW[i].print("Your turn>" + m_nl);
								m_arW[i].flush();
								strCmd = m_arR[i].readLine();
							}else
								strCmd = "";
								
					}  // END SWITCH
						
//					strCmd = m_arR[i].readLine();
					if( isOk && ( strCmd != null ) )
					{
						if( !strCmd.toLowerCase().startsWith("/quit") )
						{
							if( strCmd != "" )
							//if( (cmd.userId == m_userToAccept) || ( m_state == ServerState.Battle && ( (m_cBattle.attackerId-1 == i) || (m_cBattle.defenderId-1 == i) ) ) )
							{
								System.out.println( (i + 1) + " sends " + strCmd );
								cmd = m_cProtocol.ParseString(strCmd);
								System.out.println("Command is: " + cmd);
								switch( cmd.action )
								{
									case Play:
										if( (cmd.targetId == 0)/* || ( cmd.targetId == m_userToAccept ) */)
										{
											PlayCard(cmd);
										}else
										{
											// This method usually raise a war. So <m_state> will be Battle
											PlayCardOnUser(cmd);
										}
										break;
									case Take:
										if( cmd.targetId == 0 )
											GiveCard(cmd);
										break;
									case Pass:
										System.out.println("User #" + cmd.userId + " pass");
										break;
									case Dump:
										DumpInfo(cmd);
										break;
								}  // END SWITCH
							}  // END IF command is from current user
						}else
						{
							System.out.println("User #" + (i+1) + " ends game!");
							isOk = false;
						}  // END ELSE IF user sends /quit
					}else
					{
						isOk = false;
					}  // END ELSE IF
				}  // END FOR
			}catch( Exception ex )
			{
				ex.printStackTrace();
				if( limit-- < 1 )
					isOk = false;
			}
			
			if( m_state == ServerState.CountDown && m_turnsLeft-- < 0 )
			{
				isOk = false;
			}

			
		}  // END WHILE
		System.out.println("Server ends...");
		try
		{
			
			for (int i = 0; i < m_totalUsers; i++)
			{
				m_arR[i].close();
				m_arW[i].close();
				m_arSockets[i].close();
			}
		}catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		return true;
	}

	private boolean WaitForUsers()
	{
		try
		{
			Socket owner;
			BufferedReader bufR;
			String msg;
			// Users to wait after game owner
			int usersToWait;
			
			System.out.println("Waiting for game owner to connect");
			owner = m_socket.accept();
			bufR = new BufferedReader( new InputStreamReader( owner.getInputStream() ) );
			msg = bufR.readLine();
			if( (msg == null) || (msg.length() < 1) )
			{
				System.out.println("Bad input from game owner!");
				return false;
			}
			
			System.out.println("Waiting for " + msg + " to connect");
			usersToWait = Integer.parseInt(msg);
			
			//System.out.println("Waiting for " + m_totalUsers + " to connect");

			InitUserCommunications(usersToWait + 1);
			for( int i = 0; i < m_totalUsers; i++ )
			{
				m_arSockets[i] = m_socket.accept();
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("done");
		return true;
	}

	private void PlayCard(Command cmd)
	{
		Card c = m_cardSet.Get(cmd.cardId);
		//System.out.println("User #" + cmd.userId + " is playing " + cmd.cardId + " on himself");
		System.out.println("Playing " + c + " on himself");
		
		// Let's build town
		switch( c.type )
		{
			case Town:
				m_arBuildings[m_nBuildings] = new CardTown( c.cardId, c.name, c.glory);
				m_arBuildings[m_nBuildings].userId = cmd.userId;
				//m_arW[cmd.userId-1].print(m_arBuildings[m_nBuildings]);
				//m_arW[cmd.userId-1].flush();
				BroadCast(m_arBuildings[m_nBuildings] + m_nl);
				m_nBuildings++;
				break;
//			case Building:
//				int pos = -1;
//				for (int i = 0; i < m_arBuildings.length; i++)
//				{
//					if( m_arBuildings[i].cardId == cmd.targetId )
//						pos = i;
//				}
//				if( pos != -1 )
//				{
//					System.out.println("Building!");
//					m_arBuildings[pos].Build(c.name, c.glory);
//					m_arW[cmd.userId-1].print(m_arBuildings[pos]);
//					m_arW[cmd.userId-1].flush();
//				}
//				break;
		}  // END SWITCH
	}

	private void GiveCard(Command cmd)
	{
		String str = "";
		int cardId = -1;
		int cardsQnt = 0;							// How many cards we gave
		
		System.out.println("User #" + cmd.userId + " takes " + cmd.quantity + " cards from CardSet");
		for (int i = 0; i < cmd.quantity; i++)
		{
			cardId = m_cardSet.RandomId(cmd.userId);
			if( cardId == 0 )
			{
				StartCountDown();
			}else
			{
				str = "Card ID is " + cardId + m_cardSet.Get(cardId) + "\n";
				BroadCast(str);
//				m_arW[cmd.userId - 1].print(str);
//				m_arW[cmd.userId - 1].flush();
				cardsQnt++;
			}
		}  // END FOR
		
//		if( cardsQnt > 0 )
//		{
//			m_arW[cmd.userId - 1].print(str);
//			m_arW[cmd.userId - 1].flush();
//		}
	}

	private void PlayCardOnUser(Command cmd)
	{
		Card c = m_cardSet.Get(cmd.cardId);
		Card cTarget = m_cardSet.Get(cmd.targetId);
		// Position of built town
		int pos = -1;
		
		System.out.println("Playing " + c );
		switch( c.type )
		{
			case Event:
				if( c.ActsOn() == "Town" )
				{
					PlayEventOnTown(cmd, c);
				}
				break;
			case Chronicles:
				
				break;
			case Unit:
				PlayBattle( cmd );
				break;
			case WoodWall:
			case BrickWall:
				pos = -1;
				for (int i = 0; i < m_nBuildings; i++)
				{
					if( m_arBuildings[i].cardId == cmd.targetId )
						pos = i;
				}
				if( pos != -1 )
				{
					System.out.println("Building: " + c);
					if( m_arBuildings[pos].BuildWall( c ) )
					{
						m_arW[cmd.userId-1].print(m_arBuildings[pos]);
						m_arW[cmd.userId-1].flush();
					}else
					{
						System.out.println("Error building");
					}
				}
				break;
			case Building:
				pos = -1;
				for (int i = 0; i < m_nBuildings; i++)
				{
					if( m_arBuildings[i].cardId == cmd.targetId )
						pos = i;
				}
				if( pos != -1 )
				{
					System.out.println("Building: " + c);
					if( m_arBuildings[pos].Build(c.name, c.glory) )
					{
						BroadCast(m_arBuildings[pos] + m_nl);
						//m_arW[cmd.userId-1].print(m_arBuildings[pos]);
						//m_arW[cmd.userId-1].flush();
					}else
					{
						System.out.println("Error building");
					}
				}
				break;
		}  // END SWITCH
	}

	private void StartCountDown()
	{
		if( m_state == ServerState.CountDown )
			return;
		
		m_state = ServerState.CountDown;
		m_turnsLeft = m_totalUsers;
		//System.out.println("There are " +);
	}
	
	private void BroadCast( String msg/*, PrintWriter[] arW */)
	{
		for (int i = 0; i < m_arW.length; i++)
		{
			m_arW[i].print(msg);
			m_arW[i].flush();
		}
	}

	private void DumpInfo(Command cmd)
	{
		String msg = "";
		String newLine =  System.getProperty("line.separator");
		
		switch( cmd.dumpId )
		{
			case Towns:
				if( m_nBuildings > 0 )
				{
					for (int i = 0; i < m_nBuildings; i++)
						msg += m_arBuildings[i].toString() + newLine;
					
					m_arW[cmd.userId-1].print(msg);
					m_arW[cmd.userId-1].flush();
				}
				break;
		}
	}

	private void ChangeTownOwner(Card cTarget, int userId)
	{
//		String str = "";
		//System.out.println("Town " + cTarget.name + " moved from " + cTarget.userId + " to user " + userId );
		BroadCast("Town " + cTarget.name + " now belongs to user " + userId + m_nl);
//		str = cTarget.cardId + "-" + cTarget.userId;
		m_arOwners.add( cTarget.cardId + "-" + cTarget.userId );
		cTarget.userId = userId;
	}
	
	/** Returns pos in array of built towns or -1 */
	private int GetBuiltTownPos(int townId)
	{
		CardTown cTown = null;
		int pos = -1;
		int i = 0;
		
		while( (pos == -1) && ( i < m_nBuildings ) )
		{
			if( m_arBuildings[i].cardId == townId)
				pos = i;
			i++;
		}
		
		return pos;
	}

	private CardTypes PlayBattle( Command cmd )
	{
		String strCmd;
		boolean isOk = true;
		Card attackerCard = m_cardSet.Get(cmd.cardId);
		Card defenderCard = null;
		//Card cTarget = m_cardSet.Get(cmd.targetId);
		int pos = GetBuiltTownPos(cmd.targetId);
		int attackerS = 0;
		int defenderS = 0;
		
		if( pos != -1 )
		{
			//BroadCast("Attacker ID=" + cmd.userId + " defender ID=" + m_arBuildings[pos].userId + m_nl);
			attackerS = cmd.userId - 1;
			defenderS = m_arBuildings[pos].userId - 1;

			while( isOk )
			{
				try
				{
					BroadCast("" + m_arBuildings[pos] + " is under attack of " + attackerCard + m_nl );

					m_arW[defenderS].print("Defend>" + m_nl);
					m_arW[defenderS].flush();
					strCmd = m_arR[defenderS].readLine();
					cmd = m_cProtocol.ParseString(strCmd);
//					if( (cmd.action == CommandAction.Play) )
//					{
					defenderCard = m_cardSet.Get(cmd.cardId);
					BroadCast("Defedns using " + defenderCard + m_nl );
					if( defenderCard.strength > attackerCard.strength )
					{
						BroadCast( defenderCard + " beats " + attackerCard );
						
						// Reads attacker turn
						m_arW[attackerS].print("Attack>" + m_nl);
						m_arW[attackerS].flush();
						strCmd = m_arR[attackerS].readLine();
						cmd = m_cProtocol.ParseString(strCmd);
						attackerCard = m_cardSet.Get(cmd.cardId);
					}else
					{
						ChangeTownOwner( m_arBuildings[pos], attackerS+1 );
						isOk = false;
					}  // END IF defend
//					}else
//					{
//						ChangeTownOwner( m_arBuildings[pos], attackerS+1 );
//						isOk = false;
//					}
					
					//isOk = false;
					
				} catch(Exception ex)
				{
					ex.printStackTrace();
					isOk = false;
				}
			}  // END WHILE
			BroadCast("Battle is finished" + m_nl );
		}else
		{
			BroadCast("Bad town ID" + m_nl);
		}  // END IF
		return null;
	}

	private void PlayEventOnTown(Command cmd, Card c)
	{
		int pos = GetBuiltTownPos( cmd.targetId );
		if( pos == -1 )
		{
			BroadCast("User #" + cmd.userId + " played on non-existing town!" + m_nl);
			return;
		}
		
		// Town could be returned (need to ensure it belong to user)
		//		could be betrayed (check if target town has no guard - TODO)
		//		could be taken w/o any defense (decrease user glory)
		if( c.ActsAs() == "Return" )
		{
			if( WasInUserHand( cmd.targetId, cmd.userId ) )
			{
				BroadCast("Town #" + cmd.targetId + " will be returned to user #" + cmd.userId + m_nl );
			}else
			{
				BroadCast("Town #" + cmd.targetId + " was not in user #" + cmd.userId + " hand!" + m_nl );
			}
		}  // END IF
	}
	
		/** Returns true if userId had townId before */
	public boolean WasInUserHand( int townId, int userId )
	{
		boolean was = false;
		int i = 0;
		String owner = "";
		String enq = "" + townId + "-" + userId;
		
		while( (was == false) && (i < m_arOwners.size()) )
		{
			owner = (String)m_arOwners.get(i);
			if( owner == enq )
				was = true;
			i++;
		}
		
		return was;
	}

	
}  // END CLASS

