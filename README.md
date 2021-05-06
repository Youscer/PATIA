# PATIA
## Prérequis
Le projet est développé pour Java8 et plus
## Compilation
Compilation avec maven
```
mvn install
```
Sans les tests unitaire
```
mvn install -DskipTests
```

Un jar est créé dans le dossier *target/* nommé *SATPlanner-jar-with-dependencies.jar*\
Il contient toutes les dépendances à l'intérieur.

Pour générer la Javadoc dans le dossier *target/site/*
```
mvn javadoc:javadoc
```

## Lancement
Un script de lancement est disponible dans le dossier racine
```
bash launch.sh
```
### Arguments

Plusieurs arguments sont possible
Ils n'ont pas d'ordre prédéfinis

#### Domaine
* -d [dom] [prob]\
Permet de préciser un domaine [dom] et un problème [prob]\
Exemple :
```
 -d logistics p01.pddl
```
Le programe charge le domaine pddl/logistics/domain.pddl\
Et l'instancie avec le problème pddl/logistics/p01.pddl

#### Planneur
* -p [planneur]\
  Permet de préciser un planneur\
  deux options possible :
```
 -p sat
```
ou
```
 -p astar
```

#### Log
* -l [option1] [option2]...\
Permet de préciser quels log ont souhaite avoir du solveur\
deux options possible parmis :

  * plan\
  Affiche le plan si une solution est trouvé
  * steps\
  Affiches le numéro de l'étape et le nombre de clauses par étapes
  * info\
  Affiche des informations complémentaire sur le déroulement du planneur\
  Détail le temps d'encodage / de recherche
  * error\
  Affiche certaines erreurs complémentaire en cas d'echec comme un timeout
  * timings\
  Affiche le temps total que le planneur a mis, dans un format "secondes.ms"\
  Surtout utilisé pour les statistiques car le log info affiche des infos plus\
  lisible sur le temps d'execution.
  
  Exemple :
  
  ```
  -l info steps plan
  ```
  
#### Timeout
* -t [timeout]\
Met en place un timeout\
Une résolution à une étape i sera limité par le timeout\
La résolution global du problème sera limité par timeout * 2\

### Exemple de lancement
```
bash start.sh -d logistics p01.pddl -p sat -l info steps plan error -t 5
```
Lance le planneur sat sur le domain *pddl/logistics/domain.pddl* avec le problème *pddl/logistics/p01.pddl*.\
Lors de l'executions, des informations sur le déroulement seront affiché, le numéro des étapes,\
le plan de la solution et les eventuelles erreurs que le planneur rencontre.\
Le timeout est fixé à 5 secondes pour une étape SAT et à 10 secondes de timeout global

## Statistiques
Les scripts de statistiques permettent de comparer le planneur SAT avec le planneur A\*\
Ils génèrent un fichier .csv par domaine dans le repertoire  *stats/*
#### Statistiques global
Le script de statistique global s'execute depuis le répertoire racine :

```
bash allstats.sh
```
#### Statistiques individuel
Le script de statistique individuel s'execute depuis le répertoire racine :

```
bash stats.sh [domaine]
```

où *[domaine]* est le nom du répertoire contenu dans *pddl/* \
par exemple :

```
bash stats.sh logistics
```

## PDDL supplémentaire
Trois domaines avec deux problèmes très basique ont été ajouté dans le dossier *pddl/*
* simple
* simple1
* simple2

Les tests sont effectué sur ces domaines
