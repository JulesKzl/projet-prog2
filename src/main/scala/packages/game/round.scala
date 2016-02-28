package packages
package game

import map._
import entities._
import sugar._



/** Gestion d'un round (vague d'attaquant).
  *
  * Pendant le round, le joueur ne peut pas ajouter de tour.
  * Il regarde les tours défendre le passage contre une vague de monstres
  * @param wave : List[Tuple2[Set[Tuple2[Monster,Int]],Int]]
  */
class Round(wave:List[Tuple2[Set[Tuple2[MonsterType,Int]],Int]]) {

  /** Nombre de vies restantes, à initialiser avec Level.life et à renvoyer à Level en fin de round */
  var lives = 0

  /** Nombre de tick depuis le début du round */
  var compteur_tick = 0

  /** Ensemble des monstres en vie */
  var monsters = Set[Monster] ()

  /** Fonction d'aide au tri : insère un élément dans la liste de façon à ce qu'elle reste triée */
  def insertion (l:List[Tuple2[Set[Tuple2[MonsterType,Int]],Int]] , x:Tuple2[Set[Tuple2[MonsterType,Int]],Int]) : List[Tuple2[Set[Tuple2[MonsterType,Int]],Int]] = {
    if (l.isEmpty) {List(x)}
    else {if (l.head._2 < x._2) {(l.head) :: (insertion (l.tail,x))}
          else  {if (l.head._2 == x._2) {l} // cas pathologique qui n'est pas censé arriver
		 else {x :: l}
                }
         }
  }

  /** Fonction qui ordonne la liste des vagues d'attaquants (tri par insertion vu le faible nombre de vagues qui vont arriver) */
  def sort_tupple_list (l:List[Tuple2[Set[Tuple2[MonsterType,Int]],Int]]) = {
    var l_sorted = List[Tuple2[Set[Tuple2[MonsterType,Int]],Int]] ()
    var l_not_sorted = l
    for (k <- 1 to l.size) {l_sorted = insertion (l_sorted,l_not_sorted.head) ; l_not_sorted = l_not_sorted.tail}
    l_sorted
  }

  /** Les différentes vagues d'attaquants */
  var waves = sort_tupple_list (wave)

  def rem_monster (monster:Monster) : Unit = {
    monsters = monsters - monster
    Map.remove_monster(monster,monster.pos)
  }

  /** Fonction auxiliaire utilisée pour les vagues */
  def add_monsters_waves (t:MonsterType,n:Int) : Unit = {
    var k = 1
    for (k <- 1 to n) {var m = t.get_instance() ; Map.new_monster (m) ; monsters = monsters + m}
  }

  /** Actualise l'état de l'objet.
    *
    *  Cette méthode doit être appelée à chaque tick
    */
  def actualize (): Unit = {
    var l = 0
    var c = 0

  /* ici on ajoute dans le set de monstres une éventuelle nouvelle vague */
  if (!waves.isEmpty) // pour pas bugger sur la commande suivante
    {
     if (waves.head._2*1000/tick == compteur_tick) // s'il est temps
       {
	 for (x <- waves.head._1) {add_monsters_waves(x._1,x._2)}
         waves = waves.tail
       }
    }


  /* cette première boucle parcourt la map, trouve les tours et les fait tirer  */
    for (l <- 0 to Map.height - 1 ; c <- 0 to Map.width - 1)
    {
      val p = new Position(l,c)
      if (Map.is_tower (p))
      {
        var killed = (Map.get_tower (p)).apply
        killed.foreach {rem_monster}
      }
      else
      {}

    }

  /* ici on parcourt les monstres : on les fait avancer, et éventuellement enlever une vie au joueur */
    monsters.foreach { (m:Monster) => if (m.apply) {lives = lives-1} }

    is_finished ()
    compteur_tick = compteur_tick + 1
  }


  /** Permet de savoir quand le round est terminé
    *
    *  @return true si le round est fini, false sinon.
    */
  def is_finished () : Boolean = {(monsters.isEmpty && waves.isEmpty) || (lives == 0)}
}
