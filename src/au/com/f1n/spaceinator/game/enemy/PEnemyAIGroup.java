package au.com.f1n.spaceinator.game.enemy;

import java.io.Serializable;

/**
 * This class enables group AI movement and fire.
 * 
 * @author luke
 * 
 */
public interface PEnemyAIGroup extends Serializable {

	boolean timeStep(int dTime, long timeS);

	boolean add(PEnemy newEnemy);

	void killed(PEnemy pEnemy);

}
