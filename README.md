Réponse au cahier des charges.
Outils utilisés
Pour répondre au " java technical test ", j'ai choisi d'utiliser, parmi ceux proposé, les outils 
suivants :
*	OpenJdk (23.0.2)
*	Eclipse IDE for Java Developers (4.34.0)
*	Postgresql (15)
*	Jdbc postgresql (postgresql-42.7.5.jar)
*	Maven
Base de Données
Pour la création de la Base de Données PostgreSql, j'ai utilisé le script suivant :
Création de la base de données
CREATE DATABASE db_location;
Connexion à la base de données
\c db_location;
Création de la table 'location'
CREATE TABLE location (
id SERIAL PRIMARY KEY,
license_plate VARCHAR(10) NOT NULL,
brand TEXT,
model TEXT,
status TEXT NOT NULL
);
Trigger sur maj de la table
  CREATE OR REPLACE FUNCTION notify_update_location() RETURNS trigger AS $$
   BEGIN
       PERFORM pg_notify('update_location_channel', row_to_json(NEW)::text);
       RETURN NEW;
   END;
   $$ LANGUAGE plpgsql;
   CREATE TRIGGER update_location_trigger AFTER UPDATE ON location
   FOR EACH ROW EXECUTE PROCEDURE notify_update_location ();
Insertion de données dans la table 'location'
INSERT INTO location (license_plate, brand, model, status)
VALUES
('3730ZG27', 'peugeot', '107', 'AVAILABLE'),
('AJ-991-JZ', 'peugeot', 'partner', 'RENTED'),
('FP-030-MR', 'volkswagen', 'golf', 'AVAILABLE'),
('EH-551-ZY', 'volkswagen', 'passat', 'AVAILABLE') ;
Requête de lecture des enregistrements
SELECT license_plate, brand, model, status FROM location;
Requête de maj du statut du véhicule
UPDATE location SET status = 'RENTED' WHERE status = 'AVAILABLE' AND license_plate = 'EH-
551-ZY'
Projet eclipse
Création d'un projet " simple " maven sous eclipse.
Ajout du driver Jdbc PostgreSQL en tant que jar externe.
IHM
Le programme est composé de plusieurs fichiers sources
*	Main.java
*	FenetrePrincipale.java
*	BdD.java
Main.java
Le fichier main.java instancie la FenetrePrincipale et l'affiche.
BdD.java
Le fichier BdD.java établi la connexion avec la base de données PostgreSQL, contrôle toutes les 
5 secondes la présence due à une notification venant du trigger PostgreSql qui lui-même est 
déclenché sur un " update " de la table location. Cette classe permet aussi la lecture de la table 
location ainsi que sa misa à jour.
FenetrePrincipale.java
Cette fenêtre affiche un tableau. Ce tableau contient les informations sur les véhicules. Ces 
informations sont obtenues par le résultat d'une requête SQL à la base de données. Les 
colonnes du tableau correspondent à chacun des champs de la table " location ".
Chaque tuple renvoyé par la base de données est affiché dans une ligne du tableau. Les champs 
" license plate ", " brand ", " model ", apparaissent sous forme de chaine de caractères alors que 
le champ " status " apparait sous le label d'un JButton.
Pour changer le statut d'un véhicule il faut cliquer sur le bouton de la ligne dans le tableau 
correspondant au véhicule en question. Le clic sur le bouton déclenche un " update " dans la 
base de données. Si le requête ne peux aboutir une fenêtre " pop-up " informe l'utilisateur. La 
requête " update " contient une condition qui vérifie que le statut actuel du véhicule est toujours 
dans l'état inverse de celui voulu par l'utilisateur.
Lorsqu'une requête " update " est effectué en base de données un trigger est déclenché. Le 
" pulling " sur la notification en informe l'IHM, ce qui déclenche à son tour une maj du tableau. 
Si plusieurs instances du programme sont lancées, elles seront toutes mise à jour.
Avec ses deux conditions, le système permet de gérer la concurrence des accès à la base de 
données. 
