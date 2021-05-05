# PATIA
### Prérequis
Le projet est développé pour Java8 et plus
### Compilation
Compilation avec maven
```
mvn install
```
Sans les tests unitaire
```
mvn install -DskipTests
```
Un jar est créé dans le dossier "target/" nommé "SATPlanner-jar-with-dependencies.jar"\
Il contient toutes les dépendances à l'intérieur
### Lancement
Un script de lancement est disponible dans le dossier racine
```
bash launch.sh [domain_path] [problem_path]
```
### PDDL supplémentaire
Trois domaines avec deux problèmes très basique ont été ajouté dans le dossier "pddl/"
* simple
* simple1
* simple2\
Les tests sont effectué sur ces domaines
