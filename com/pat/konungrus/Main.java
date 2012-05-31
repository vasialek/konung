/*
 * Main.java
 *
 * Created on 2007-01-29
 *
 */

package com.pat.konungrus;

/**
 *
 * @author Aleksej V.
 */
public class Main
{
    
    /** Creates a new instance of Main */
    public Main()
    {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
//		Server s = new Server();
//		s.Run();

		CardSet cardSet = new CardSet();
		Card c = null;
		System.out.println( cardSet.toString() );

		
		UserHand cUser = new UserHand(cardSet, 2);
		int n = 3;
		
		cardSet.Remove(4);
		cardSet.Remove(11);
		
		System.out.println(cUser);
		System.out.println("Taking " + n + " cards");
		cUser.TakeCards(n);
		System.out.println(cUser);
		
		System.out.println(cardSet.toString());

//		CardTown cTown = new CardTown("Kiev", 3);
//		
//		cTown.Build("Church", 1);
//		cTown.BuildWall( 2, WallTypes.Paling );
//		System.out.println(cTown);

//		// Check events in game
//		c = cardSet.Get(41);
//		System.out.println("41: " + c.ActsOn() + " " + c.ActsAs());
//		c = cardSet.Get(42);
//		System.out.println("42: " + c.ActsOn() + " " + c.ActsAs());
//		c = cardSet.Get(43);
//		System.out.println("43: " + c.ActsOn() + " " + c.ActsAs());


//		int cardId = -1;
//		int limit = 200;
//		while( cardId != 0 && --limit > 0 )
//		{
//			cardId = cardSet.RandomId(1);
//			System.out.println( cardSet.Get(cardId) );
//		}
		
//		System.out.println("1st card is: " + cardSet.Get( 1 ) );
//		System.out.println("Last card is: " + cardSet.Get( cardSet.totalCards ) );
    }
    
}
