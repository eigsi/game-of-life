import eigsi.*; // ne pas supprimer

public class Main {

  // seul le code de cette fonction est exécuté
  public static void main(String[] args) {
    configurer(50, 14);
    baseJour1();

  }

  static void configurer(int A, int B) {
    Configuration.TITRE = "Carte";
    Configuration.IMAGE_TAILLE = A; // en pixels
    Configuration.CELLULE_TAILLE_POLICE = B;
    Configuration.CELLULE_VITALLITE_COULEUR = Couleur.BLEU;
    Configuration.CELLULE_INFECTION_COULEUR = Couleur.ROUGE;
    Configuration.LARGEUR_INFORMATION_DROITE = 300;
  }

  // Code fourni montrant l'utilisation d'une partie des fonctions disponibles.
  static void baseJour1() {
    // Lecture d'une carte (selon un formatage approprié)
    System.out.println("Saisir le numero de carte (1, 2 , 3 , 4 , 5 , 6 ou 7)");
    String num = Console.saisirChaine();
    if (num == "7") {
      configurer(10, 6);
    }
    Outils.chargerCarte("carte0" + num + ".txt");
    int[][] new_etats = Outils.obtenirEtats();
    int[][] new_vitalites = Outils.obtenirVitalites();
    int[][] new_infections = Outils.obtenirInfections();
    int[][] vitalites = Outils.clonerMatrice(new_vitalites);
    int[][] etats = Outils.clonerMatrice(new_etats);
    int[][] infections = Outils.clonerMatrice(new_infections);
    int[][] vitalitesIni = Outils.clonerMatrice(vitalites);

    // Association de la matrice à la fenêtre graphique
    FenetreGraphique.initialiserFenetre(new_etats, new_vitalites, new_infections);
    // initialisation temps
    int t = 0;
    int x = etats.length;
    int y = etats[0].length;
    int tot_cell = x * y;
    int nb_immu = compteurImmunises(etats);
    int nb_non_immu = tot_cell - nb_immu;
    int pourc_non_immu = 100 * nb_non_immu / tot_cell;
    System.out.println("Saisir la valeur d'un nombre entier S ");
    int S = Console.saisirEntier();
    System.out.println("Saisir un nombre entier a pour l'attente avant les soins");
    int attente = Console.saisirEntier();
    System.out.println("Saisir le nombre de soins possibles par tour A ");
    int A = Console.saisirEntier();

    System.out.println("Saisir la valeur d'un nombre entier N ");
    int N = Console.saisirEntier();
    int[][] MODIF = new int[x][y];
    for (int i = 0; i < x; i++) {
      for (int j = 0; j < y; j++) {
        MODIF[i][j] = 0;
      }
      ;
    }
    ;
    int nb_vit = sommeVitalites(vitalites);
    int nb_virus = sommeVirus(infections);
    int nb_modif = sommeInfections(MODIF);
    int nb_sain = nb_cell_saine(vitalites, infections);
    int pourc_sain = 100 * nb_sain / tot_cell;
    int nb_infect = compteurInfectees(infections);
    int pourc_infect = 100 * nb_infect / tot_cell;
    int nb_mort = cellules_mortes(vitalites, etats);
    int pourc_mort = 100 * nb_mort / tot_cell;
    boolean auto;
    System.out.println("Choisir le mode de medecine : 1 pour medecine automatique, 2 pour medecine manuel");
    int Autom = Console.saisirEntier();
    auto = (Autom == 1);
    System.out.println("Choisir la forme de carte : 1 pour mode bouee, 2 pour mode plan");
    int BOUEE = Console.saisirEntier();
    boolean boue = (BOUEE == 1);

    FenetreGraphique.modifierMessageDroite("Informations:\\n \\nNombre totale de cellule: " + tot_cell
        + "\\nNombre de cellule non immunisée: " + nb_non_immu + " (" + pourc_non_immu + "%)\\nIl y a " + nb_infect
        + " cellules infectées (" + pourc_infect + "%)\\nIl y a " + nb_sain + " cellules saines (" + pourc_sain
        + "%)\\nIl y a " + nb_mort + " cellules mortes (" + pourc_mort + "%)\\nIl y a " + nb_virus + " virus\\nIl y a "
        + nb_vit + " points de vitalité\\nt = " + t);

    FenetreGraphique.modifierMessageBas(
        "Attente avant les soins: " + attente + "; Medecine auto: " + auto + " ; N= " + N + " ; S= " + S);

    changerCouleur(x, y, vitalitesIni, infections);
    rafraichirCellules(etats);

    // Boucle principale
    while (nb_virus != 0 || t < attente + 2 || nb_modif != 0) {
      System.out.print("Appuyer sur Entrer pour suite");
      Console.appuyerSurEntrer();

      // Evolution du temps
      t++;
      int t_rest = attente - t;
      if (t_rest > 0) {
        FenetreGraphique.modifierMessageBas(
            "Attente restante avant les soins: " + t_rest + " ; Medecine auto: " + auto + " ; N= " + N + " ; S= " + S);
      }
      if (t_rest <= 0 && auto) {
        FenetreGraphique.modifierMessageBas("Soins en cours ; Medecine auto: " + auto + " ; N= " + N + " ; S= " + S);
      }

      int[][] infectionsVoisines = calculerInfectionsVoisines(infections, etats, boue);
      int[][] vitalitesVoisines = calculerVitalitesVoisines(infections, etats, vitalites, boue);

      // Evolution virus
      for (int i = 0; i < x; i++) {
        for (int j = 0; j < y; j++) {
          int[] New_tab = evo_virus(i, j, etats, infections, vitalites, t);
          new_etats[i][j] = New_tab[0];
          new_vitalites[i][j] = New_tab[2];
          new_infections[i][j] = New_tab[1];
        }
      }
      ;

      // Propagation du virus
      for (int i = 0; i < x; i++) {
        for (int j = 0; j < y; j++) {
          int[] n = modifiercellule(i, j, vitalites, new_vitalites, etats, new_etats, infections, new_infections, S,
              infectionsVoisines, t);
          new_vitalites[i][j] = n[0];
          new_infections[i][j] = n[1];
          new_etats[i][j] = n[2];

        }
      }
      // afficherMatriceEnConsole(etats);
      // afficherMatriceEnConsole(new_etats);
      // regeneration naturelle

      for (int i = 0; i < x; i++) {
        for (int j = 0; j < y; j++) {
          if (MODIF[i][j] == 1) {
            MODIF[i][j] = 0;
          }
          ;
        }
        ;
      }
      ;

      for (int i = 0; i < x; i++) {
        for (int j = 0; j < y; j++) {
          int[] n = renew_cel(i, j, etats, new_etats, infections, new_infections, MODIF, vitalites, new_vitalites,
              vitalitesIni, N, vitalitesVoisines, t, attente);
          new_vitalites[i][j] = n[1];
          MODIF[i][j] = n[2];
          new_etats[i][j] = n[0];
        }
      }

      vitalites = Outils.clonerMatrice(new_vitalites);
      etats = Outils.clonerMatrice(new_etats);
      infections = Outils.clonerMatrice(new_infections);

      rafraichirCellules(new_etats);
      changerCouleur(x, y, vitalitesIni, infections);

      vitalites = Outils.clonerMatrice(new_vitalites);
      etats = Outils.clonerMatrice(new_etats);
      infections = Outils.clonerMatrice(new_infections);

      rafraichirCellules(new_etats);
      if (t >= attente && A > 0 && auto == false) {
        System.out.print("Appuyer sur Entrer pour passer aux soins manuels");
        Console.appuyerSurEntrer();
      }
      if (t >= attente && A == 0 && auto == false) {
        System.out.print("Appuyer sur Entrer pour continuer");
        Console.appuyerSurEntrer();
      }
      if (t >= attente && A > 0 && auto) {
        System.out.print("Appuyer sur Entrer pour passer aux soins automatiques");
        Console.appuyerSurEntrer();
      }
      // Update les cellules
      changerCouleur(x, y, vitalitesIni, infections);
      rafraichirCellules(new_etats);
      nb_sain = nb_cell_saine(vitalites, infections);
      pourc_sain = 100 * nb_sain / tot_cell;
      nb_infect = compteurInfectees(infections);
      pourc_infect = 100 * nb_infect / tot_cell;
      nb_mort = cellules_mortes(vitalites, etats);
      pourc_mort = 100 * nb_mort / tot_cell;
      nb_modif = sommeInfections(MODIF);
      nb_virus = sommeVirus(infections);
      nb_vit = sommeVitalites(vitalites);

      // boucle de soin
      if (t >= attente) {
        if (auto) {
          // boucle de soin automatique
          int[][] coCellulesB = coCellulesB(vitalites, etats);
          int k = 0;
          int l = 0;
          int r = 0;
          int i = 0;
          while (i < A) {
            int[][] resultatd = devitalisationAuto(new_vitalites, vitalitesIni, new_etats, coCellulesB, new_infections,
                S);
            k = resultatd[0][1];
            l = resultatd[0][2];
            r = resultatd[0][0];
            if (r == 1) {
              FenetreGraphique.encadrerElement(k, l, Couleur.NOIR, 2);
              Outils.attendre(2000);
              FenetreGraphique.supprimerCadreElement(k, l);
              FenetreGraphique.mettreCouleurFond(k, l, Couleur.ROUGE);
              vitalites = Outils.clonerMatrice(new_vitalites);
              etats = Outils.clonerMatrice(new_etats);
              infections = Outils.clonerMatrice(new_infections);
              rafraichirCellules(new_etats);
              changerCouleur(x, y, vitalitesIni, infections);
              i++;
            } else {
              int[][] resultat = regenerationAuto(new_vitalites, vitalitesIni, new_etats, coCellulesB, infections,
                  MODIF);

              k = resultat[0][1];
              l = resultat[0][2];
              r = resultat[0][0];
              if (r == 1) {
                FenetreGraphique.encadrerElement(k, l, Couleur.VERT, 2);
                Outils.attendre(2000);
                FenetreGraphique.supprimerCadreElement(k, l);
                FenetreGraphique.mettreCouleurFond(k, l, Couleur.VERT);
                vitalites = Outils.clonerMatrice(new_vitalites);
                etats = Outils.clonerMatrice(new_etats);
                infections = Outils.clonerMatrice(new_infections);
                rafraichirCellules(new_etats);
                changerCouleur(x, y, vitalitesIni, infections);
                i++;

              }
            }
            vitalites = Outils.clonerMatrice(new_vitalites);
            etats = Outils.clonerMatrice(new_etats);
            infections = Outils.clonerMatrice(new_infections);
            rafraichirCellules(etats);
            changerCouleur(x, y, vitalitesIni, infections);
            if (r != 0) {
              i++;
            }
            ;
          }
          ;

        }

        else {

          for (int i = 0; i < A; i++) {
            int nb_rest = A - i;
            System.out.println("Il reste " + nb_rest + " soins possibles");
            System.out
                .println("Saisir 1 pour procéder à une regénération, 2 pour une dévitalisation, 0 pour ne rien faire");
            FenetreGraphique.modifierMessageBas(
                "Il reste " + nb_rest + " soins possibles ; Medecine auto: " + auto + " ; N= " + N + " ; S= " + S);
            int a = Console.saisirEntier();
            if (a == 0) {
              break;
            }
            if (a == 1) {
              regeneration(new_vitalites, vitalitesIni, new_etats);
              Outils.attendre(2000);
              int k = FenetreGraphique.dernierIndexLigneClicSouris();
              int l = FenetreGraphique.dernierIndexColonneClicSouris();
              FenetreGraphique.supprimerCadreElement(k, l);
              rafraichirCellules(new_etats);
              changerCouleur(x, y, vitalitesIni, infections);
              FenetreGraphique.mettreCouleurFond(k, l, Couleur.VERT);

            } else {
              devitalisation(new_vitalites, new_etats);
              Outils.attendre(2000);
              int k = FenetreGraphique.dernierIndexLigneClicSouris();
              int l = FenetreGraphique.dernierIndexColonneClicSouris();
              FenetreGraphique.supprimerCadreElement(k, l);
              rafraichirCellules(new_etats);
              changerCouleur(x, y, vitalitesIni, infections);

            }

            vitalites = Outils.clonerMatrice(new_vitalites);
            etats = Outils.clonerMatrice(new_etats);
            infections = Outils.clonerMatrice(new_infections);
          }
          ;
        }
      }

      // affichage message

      FenetreGraphique.modifierMessageDroite("Informations:\\n \\nNombre totale de cellule: " + tot_cell
          + "\\nNombre de cellule non immunisée: " + nb_non_immu + " (" + pourc_non_immu + "%)\\nIl y a " + nb_infect
          + " cellules infectées (" + pourc_infect + "%)\\nIl y a " + nb_sain + " cellules saines (" + pourc_sain
          + "%)\\nIl y a " + nb_mort + " cellules mortes (" + pourc_mort + "%)\\nIl y a " + nb_virus
          + " virus\\nIl y a " + nb_vit + " points de vitalité\\nt = " + t);

    }

    FenetreGraphique.messagePopUp("Fin !");
  }

  // --------------------------------- FONCTIONS
  // ----------------------------------------------

  public static void afficherMatriceEnConsole(int[][] matrice) {
    if (matrice == null) {
      System.out.println("Pas d'affichage, matrice null.");
    } else {
      for (int iLig = 0; iLig < matrice.length; iLig++) { // parcours ligne par ligne
        for (int iCol = 0; iCol < matrice[iLig].length; iCol++) { // parcours colonne par colonne
          int valeur = matrice[iLig][iCol];// contenu d'une case
          System.out.print(valeur);
          System.out.print(" ");
        }
        System.out.println();
      }
    }
    System.out.println();
  }

  public static int[] evo_virus(int lig, int col, int[][] etats, int[][] infs, int[][] vits, int t) {
    int etat = etats[lig][col]; // recupere l'etat de la cellule
    int inf = infs[lig][col]; // recupere l'infection de la cellule
    int vit = vits[lig][col]; // recupere la vitalite de la cellule
    int new_inf = inf; // initialise l'infection final
    int new_etat = etat; // initialise l'etat final
    int new_vit = vit; // initialise la vitalite final
    if (inf > 0) { // verifie que la cellule est infectee
      if (vit >= inf) { // cas ou tous les virus se nourissent
        new_inf = inf + 1; // developement du virus
        new_vit = vit - inf; // perte de vitalite de la cellule
      } else { // cas ou tous les virus ne peuvent pas se nourrir
        new_vit = 0; // mort de la cellule
        new_inf = vit; // survie des virus ayant pu se nourrir
      }
      if (new_vit == 0) { // si la cellule est morte
        if (vit > 0) { // si la cellule viens de mourir
          System.out.println("la cellule [" + lig + "] [" + col + "] est morte à t=" + t);
        }
        ;
        new_etat = etat + 1; // changement de la cellule
      }
      ;
    }
    ;
    int[] new_val = { new_etat, new_inf, new_vit }; // retourne les nouvelles valeurs
    return new_val;
  };

  public static int[][] calculerInfectionsVoisines(int[][] infections, int[][] etats, boolean boue) {
    // prendre les dimensions de la carte
    int x = etats.length;
    int y = etats[0].length;
    // Compter infections voisines
    int[][] infectionsVoisinesMatrice = new int[x][y];
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        int infectionsVoisines = 0;
        if (boue) {
          for (int k = i - 1; k <= i + 1; k++) {
            for (int l = j - 1; l <= j + 1; l++) {
              int m = k;
              int n = l;
              if (k == i && l == j) {
                continue;
              }
              ;
              if (m < 0) {
                m = x - 1;
              }
              ;
              if (n < 0) {
                n = y - 1;
              }
              ;
              if (m >= x) {
                m = 0;
              }
              ;
              if (n >= y) {
                n = 0;
              }
              ;
              infectionsVoisines += infections[m][n];
            }
            ;
          }
          ;
        } else {
          for (int k = Math.max(0, i - 1); k <= Math.min(x - 1, i + 1); k++) {
            for (int l = Math.max(0, j - 1); l <= Math.min(y - 1, j + 1); l++) {
              if (k == i && l == j) {
                // Ne pas compter la cellule elle-même
                continue;
              }
              if (infections[k][l] >= 0) {
                infectionsVoisines += infections[k][l];

              }

            }
          }
        }

        infectionsVoisinesMatrice[i][j] = infectionsVoisines;
      }
    }
    // System.out.println("Matrice des infections voisines : ");
    // afficherMatriceEnConsole(infectionsVoisinesMatrice);
    return infectionsVoisinesMatrice;
  };

  public static void rafraichirCellules(int[][] etats) {
    for (int i = 0; i < etats.length; i++) {
      for (int j = 0; j < etats[i].length; j++) {
        FenetreGraphique.rafraichirElement(i, j);
      }
    }
  };

  // calcule la somme d'infections en comptant les soins
  public static int sommeInfections(int[][] infections) {
    int sum = 0;
    for (int i = 0; i < infections.length; i++) {
      for (int j = 0; j < infections[i].length; j++) {
        sum += infections[i][j];
      }
    }
    return sum;
  };

  // compte le nombre de virus restants
  public static int sommeVirus(int[][] infections) {
    int sum = 0;
    for (int i = 0; i < infections.length; i++) {
      for (int j = 0; j < infections[i].length; j++) {
        if (infections[i][j] >= 0) {
          sum += infections[i][j];
        }

      }
    }
    return sum;

  };

  // compter nombre de vitalités
  public static int sommeVitalites(int[][] vitalites) {
    int sum = 0;
    for (int i = 0; i < vitalites.length; i++) {
      for (int j = 0; j < vitalites[i].length; j++) {
        sum += vitalites[i][j];
      }
    }
    return sum;
  };

  // modifier les cellules
  static int[] modifiercellule(int lig, int col, int[][] vits, int[][] n_vits, int[][] etats, int[][] n_etat,
      int[][] infs, int[][] n_inf, int S, int[][] infectionsvoisines, int t) {
    // Niveaux de vitalité, d'infection et état des cellules au départ
    int vit = vits[lig][col];
    int etat = etats[lig][col];
    int inf = infs[lig][col];
    // Niveaux après identification des conditions
    int new_vit = n_vits[lig][col];
    int new_etat = n_etat[lig][col];
    int new_inf = n_inf[lig][col];
    int nb_inf = infectionsvoisines[lig][col];
    // Conditions
    if (inf == 0) {
      if (etat != 9 && etat != 11) {
        if (vit > 0) {
          if (nb_inf >= S) {
            new_inf = inf + 1;
            new_vit = vit - 1;
            new_etat = etat + 1;
            System.out.println("La cellule [" + lig + "][" + col + "] a été infectée à t=" + t);
            if (new_vit == 0) {
              new_etat = new_etat + 1;
              System.out.println("la cellule [" + lig + "] [" + col + "] est morte à t=" + t);
            }
          }
          ;
        }
        ;

      }
      ;
    }
    ;
    int[] new_val = { new_vit, new_inf, new_etat };
    return new_val;

  };

  // Changement de couleur
  public static void changerCouleur(int x, int y, int[][] vitalite_init, int[][] infections) {
    for (int i = 0; i < x; i++) {
      for (int j = 0; j < y; j++) {
        double a = vitalite_init[i][j];
        double b = infections[i][j];
        double taux = b / a; // pas de vitalité
        // if (a != 0) {
        // taux = b / a;
        // }
        if (a == 0 && b == 0) {
          taux = 0; // cellule morte
        }
        if (taux == 0) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.BLANC);
        }

        if (taux < 0.2 && taux > 0) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.FOND_1);
        }
        if (taux >= 0.2 && taux < 0.35) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.FOND_2);
        }
        if (taux >= 0.35 && taux < 0.5) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.FOND_3);
        }
        if (taux >= 0.5) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.FOND_4);
        }
        if (b == -100) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.VERT);
        }
        // pour carte bonus (Les cellules B sont placées au niveau du cerveau)
        if (a == 100) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.ROUGE);
        }
        if (a == 222) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.JAUNE);
        }
        if (a == 50) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.BLEU);
        }
        if (a == 80) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.NOIR);
        }
        if (b == 150) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.ORANGE);// MARRON
        }
        if (a == 300) {
          FenetreGraphique.mettreCouleurFond(i, j, Couleur.ROSE);// BEIGE
        }
      }
    }

  }

  // fonction qui compte le nombre de cellules infectées
  static int compteurInfectees(int[][] infections) {
    int compteur = 0;
    for (int i = 0; i < infections.length; i++) {
      for (int j = 0; j < infections[i].length; j++) {
        if (infections[i][j] >= 1) {
          compteur++;
        }
        ;
      }
      ;
    }
    ;
    return compteur;
  };

  // fonction qui compte le nombre de cellules saines
  static int nb_cell_saine(int[][] vits, int[][] infs) {
    int nb = 0;
    for (int iLig = 0; iLig < vits.length; iLig++) {
      for (int icol = 0; icol < vits[iLig].length; icol++) {
        if (vits[iLig][icol] > 0 && infs[iLig][icol] == 0) {
          nb = nb + 1;
        }
        ;
      }
      ;
    }
    ;
    return nb;
  };

  // Compter le nbre de cellules mortes
  static int cellules_mortes(int[][] vit, int[][] etat) {
    int nbre_mortes = 0;
    int i;
    int j;
    for (i = 0; i < vit.length; i++) {
      for (j = 0; j < vit[0].length; j++) {
        if (vit[i][j] == 0 && etat[i][j] != 11) {
          nbre_mortes += 1;

        }
        ;
      }
      ;
    }
    ;
    return nbre_mortes;

  };

  public static int[][] calculerVitalitesVoisines(int[][] infections, int[][] etats, int[][] vitalites, boolean boue) {
    // prendre les dimensions de la carte
    int x = etats.length;
    int y = etats[0].length;
    // Compter infections voisines
    int[][] vitalitesVoisinesMatrice = new int[x][y];
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        int vitalitesVoisines = 0;
        if (boue) {
          for (int k = i - 1; k <= i + 1; k++) {
            for (int l = j - 1; l <= j + 1; l++) {
              int m = k;
              int n = l;
              if (m < 0) {
                m = x - 1;
              }
              if (n < 0) {
                n = y - 1;
              }
              if (m >= x) {
                m = 0;
              }
              if (n >= y) {
                n = 0;
              }
              if (m == i && n == j) {
                // Ne pas compter la cellule elle-même
                continue;
              }
              if (infections[m][n] < 1 && vitalites[m][n] > 0) {
                vitalitesVoisines++;
              }
            }
          }
          vitalitesVoisinesMatrice[i][j] = vitalitesVoisines;

        } else {
          for (int k = Math.max(0, i - 1); k <= Math.min(x - 1, i + 1); k++) {
            for (int l = Math.max(0, j - 1); l <= Math.min(y - 1, j + 1); l++) {
              if (k == i && l == j) {
                // Ne pas compter la cellule elle-même
                continue;
              }
              if (infections[k][l] < 1 && vitalites[k][l] > 0) {
                vitalitesVoisines++;
              }
            }
          }
          vitalitesVoisinesMatrice[i][j] = vitalitesVoisines;
        }
      }
    }
    // System.out.println("Matrice des cellules imunisées voisines : ");
    // afficherMatriceEnConsole(vitalitesVoisinesMatrice);
    return vitalitesVoisinesMatrice;
  };

  // fonction qui compte le nombre de cellules immunisées
  static int compteurImmunises(int[][] etats) {
    int compteurimmunis = 0;
    for (int i = 0; i < etats.length; i++) {
      for (int j = 0; j < etats[i].length; j++) {
        if (etats[i][j] == 9) {
          compteurimmunis++;
        }
        ;
      }
      ;
    }
    ;
    return compteurimmunis;
  };

  // Regénerer cellules
  static int[] renew_cel(int lig, int col, int[][] etats, int[][] n_etats, int[][] infections, int[][] n_infections,
      int[][] MODIF, int[][] vitalites, int[][] n_vits, int[][] vitalites_init, int N, int[][] vitvoisines, int t,
      int a) {
    int etat = etats[lig][col];
    int modiff = MODIF[lig][col];
    int inf = infections[lig][col];
    int vit = vitalites[lig][col];
    int vit_init = vitalites_init[lig][col];
    System.out.println(vit_init);
    //
    int new_etat = n_etats[lig][col];
    int new_modif = modiff;
    int new_vit = n_vits[lig][col];
    int nbre_vit = vitvoisines[lig][col];
    // Conditions
    if (t >= a) {
      if (etat != 11) {

        if (vit == 0) {// Si la cellule est morte
          if (inf == 0) {// et non infectée
            if (nbre_vit >= N) {
              new_etat = etat - 3;
              new_modif = 1;
              FenetreGraphique.mettreCouleurFond(lig, col, Couleur.VERT);
              FenetreGraphique.rafraichirElement(lig, col);
              new_vit = vit_init;// vitalité initiale
              if (new_vit == 0) {
                new_vit++;
              }
              System.out.println("La cellule [" + lig + "][" + col + "] a été regénérée à t=" + t);
            }
            ;
          }
          ;
        }
        ;

      }
      ;
    }
    int[] new_cell = { new_etat, new_vit, new_modif };
    return new_cell;

  };

  // dévitalisation d'une cellule
  public static void devitalisation(int[][] vitalites, int[][] etats) {
    FenetreGraphique.activerClicSouris(true);
    System.out.println("Cliquer sur la cellule à dévitaliser puis appuyer sur Entrer");
    Console.appuyerSurEntrer();
    int i = FenetreGraphique.dernierIndexLigneClicSouris();
    int j = FenetreGraphique.dernierIndexColonneClicSouris();
    FenetreGraphique.encadrerElement(i, j, Couleur.NOIR, 2);
    vitalites[i][j] = 0;
    if (etats[i][j] == 1 || etats[i][j] == 5) {
      etats[i][j] += 3;
    }
    ;
    if (etats[i][j] == 2 || etats[i][j] == 6 || etats[i][j] == 9) {
      etats[i][j] += 1;
    }
    ;
    FenetreGraphique.supprimerCadreElement(i, j);
  };

  // régénération d'une cellule
  public static void regeneration(int[][] vitalites, int[][] vitalitesIni, int[][] etats) {
    FenetreGraphique.activerClicSouris(true);
    System.out.println("Cliquer sur la cellule à régénérer puis appuyer sur Entrer");
    Console.appuyerSurEntrer();
    int i = FenetreGraphique.dernierIndexLigneClicSouris();
    int j = FenetreGraphique.dernierIndexColonneClicSouris();
    FenetreGraphique.encadrerElement(i, j, Couleur.VERT, 2);
    vitalites[i][j] = vitalitesIni[i][j];
    regenerationCell(vitalites, vitalitesIni, etats, i, j);
  };

  public static void regenerationCell(int[][] vitalites, int[][] vitalitesIni, int[][] etats, int i, int j) {
    if (vitalites[i][j] == 0 && etats[i][j] != 11) {
      vitalites[i][j]++;
    }
    if (etats[i][j] == 4 || etats[i][j] == 8) {
      etats[i][j] = etats[i][j] - 3;
    }
    if (etats[i][j] == 3 || etats[i][j] == 7 || etats[i][j] == 10) {
      etats[i][j] = etats[i][j] - 1;
    }
  };

  // -------------------------- MISSION 5 --------------------------------

  // ressort une matrice avec les coordoonées des cellules B
  public static int[][] coCellulesB(int[][] vitalites, int[][] etats) {
    int taille = 0;
    int p = 0;
    int x = vitalites.length;
    int y = vitalites[0].length;
    // nombre de cellules B
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        if (etats[i][j] >= 5 && etats[i][j] <= 8) {
          taille++;
        }
      }
    }
    int[][] cellulesB = new int[taille][2];
    // stocker coordonnées des cellules B
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        if (etats[i][j] >= 5 && etats[i][j] <= 8) {
          cellulesB[p][0] = i;
          cellulesB[p][1] = j;
          p++;
        }
      }
    }
    return cellulesB;
  }

  // ressort une matrice avec les coordoonées des cellules C
  public static int[][] coCellulesC(int[][] vitalites, int[][] etats) {
    int taille = 0;
    int p = 0;
    int x = vitalites.length;
    int y = vitalites[0].length;
    // nombre de cellules C
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        if (etats[i][j] == 9 || etats[i][j] == 10) {
          taille++;
        }
      }
    }
    int[][] cellulesC = new int[taille][2];
    // stocker coordonnées des cellules C
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        if (etats[i][j] == 9 || etats[i][j] == 10) {
          cellulesC[p][0] = i;
          cellulesC[p][1] = j;
          p++;
        }
      }
    }
    return cellulesC;
  }

  // Régénération automatique des cellules
  public static int[][] regenerationAuto(int[][] vitalites, int[][] vitalitesIni, int[][] etats, int[][] coCellulesB,
      int[][] infections, int[][] MODIF) {
    int[][] resultat = new int[1][3];
    resultat[0][0] = 0;
    int x = vitalites.length;
    int y = vitalites[0].length;
    int p = coCellulesB.length;
    boolean condition = false;
    for (int i = 0; i <= p - 1; i++) {
      int lig = coCellulesB[i][0];
      int col = coCellulesB[i][1];

      // 1 : régénérer cellule B morte
      if (etats[lig][col] == 8) {
        vitalites[lig][col] = vitalitesIni[lig][col];
        resultat[0][1] = lig;
        resultat[0][2] = col;
        etats[lig][col] = 5;
        condition = true;
        break;
      }
      // 2 : régénérer cellules voisines de B mortes non infectées
      for (int k = Math.max(0, lig - 1); k <= Math.min(x - 1, lig + 1); k++) {
        if (condition)
          break;
        for (int l = Math.max(0, col - 1); l <= Math.min(y - 1, col + 1); l++) {
          if (k == lig && l == col) {
            // Ne pas compter la cellule elle-même
            continue;
          }
          if (etats[k][l] == 4 || etats[k][l] == 8) {
            vitalites[k][l] = vitalitesIni[k][l];
            resultat[0][1] = k;
            resultat[0][2] = l;
            etats[lig][col] = etats[lig][col] - 3;
            condition = true;
            break;
          }
        }
      }
      if (condition)
        break;
    }
    ;
    // 3 : régénérer cellules A mortes non infectées
    if (condition == false) {
      for (int i = 0; i <= 50; i++) {
        int lig = Outils.entierAleatoire(0, x - 1);
        int col = Outils.entierAleatoire(0, y - 1);
        if (etats[lig][col] == 4) {
          vitalites[lig][col] = vitalitesIni[lig][col];
          resultat[0][1] = lig;
          resultat[0][2] = col;
          etats[lig][col] = etats[lig][col] - 3;
          condition = true;
        }
        if (condition)
          break;
      }
    }
    if (condition == true) {
      resultat[0][0] = 1;
    }
    if (condition) {
      MODIF[0][0] = 1;
    }
    return resultat;

  };

  // Dévitalisation automatique des cellules
  public static int[][] devitalisationAuto(int[][] vitalites, int[][] vitalitesIni, int[][] etats, int[][] coCellulesB,
      int[][] infections, int S) {
    int[][] resultat = new int[1][3];
    resultat[0][0] = 0;
    int x = vitalites.length;
    int y = vitalites[0].length;
    int p = coCellulesB.length;
    boolean condition = false;
    int infectionsVoisines = 0;
    for (int i = 0; i <= p - 1; i++) {
      int lig = coCellulesB[i][0];
      int col = coCellulesB[i][1];
      // 1 : Tuer les cellules A seines voisines de B si elle ont des voisins
      // infectées
      for (int k = Math.max(0, lig - 1); k <= Math.min(x - 1, lig + 1); k++) {
        if (condition)
          break;
        for (int l = Math.max(0, col - 1); l <= Math.min(y - 1, col + 1); l++) {
          if (condition)
            break;
          if (k == lig && l == col) {
            // Ne pas compter la cellule elle-même
            continue;
          }
          if (etats[k][l] == 1 || etats[k][l] == 5) {
            for (int m = Math.max(0, k - 1); m <= Math.min(x - 1, k + 1); m++) {
              if (condition)
                break;
              for (int n = Math.max(0, l - 1); n <= Math.min(y - 1, l + 1); n++) {
                if (m == k && n == l) {
                  // Ne pas compter la cellule elle-même
                  continue;
                }
                infectionsVoisines += infections[m][n];
              }
            }
            if (infectionsVoisines >= 1 && vitalites[k][l] != 0) {
              vitalites[k][l] = 0;
              resultat[0][1] = k;
              resultat[0][2] = l;
              etats[k][l] += 3;
              condition = true;
              break;
            }

          }
          infectionsVoisines = 0;
        }
      }
      if (condition)
        break;
    }
    ; // 1.5 : Tuer les cellules A saines si elle ont des voisins infectées
    for (int i = 0; i <= x - 1; i++) {
      if (condition)
        break;
      for (int j = 0; j <= y - 1; j++) {
        if (condition)
          break;
        if (etats[i][j] == 1) {
          for (int m = Math.max(0, i - 1); m <= Math.min(x - 1, i + 1); m++) {
            if (condition)
              break;
            for (int n = Math.max(0, j - 1); n <= Math.min(y - 1, j + 1); n++) {
              if (m == i && n == j) {
                // Ne pas compter la cellule elle-même
                continue;
              }
              infectionsVoisines += infections[m][n];
            }
          }
          if (infectionsVoisines >= 1 && vitalites[i][j] != 0) {
            vitalites[i][j] = 0;
            resultat[0][1] = i;
            resultat[0][2] = j;
            etats[i][j] += 3;
            condition = true;
            break;
          }

        }
        infectionsVoisines = 0;
      }
      if (condition)
        break;
    }
    // 2 : Tuer les cellules contaminées si leur taux de contamination < S
    for (int i = 0; i <= x - 1; i++) {
      for (int j = 0; j <= y - 1; j++) {
        if (infections[i][j] < S && infections[i][j] > 0 && vitalites[i][j] > 0) {
          vitalites[i][j] = 0;
          resultat[0][1] = i;
          resultat[0][2] = j;
          if (etats[i][j] == 2 || etats[i][j] == 6) {
            etats[i][j] += 1;
          }
          ;
          condition = true;
          break;
        }
      }
    }
    // 3 : Tuer les cellules contaminées si leur taux de contamination >= S
    for (int i = 0; i <= x - 1; i++) {
      if (condition) {
        break;
      }
      for (int j = 0; j <= y - 1; j++) {
        if (infections[i][j] >= S && vitalites[i][j] > 0) {
          vitalites[i][j] = 0;
          resultat[0][1] = i;
          resultat[0][2] = j;
          if (etats[i][j] == 2 || etats[i][j] == 6) {
            etats[i][j] += 1;
          }
          ;
          condition = true;
          break;
        }
      }
    }
    if (condition) {
      resultat[0][0] = 1;
    }
    return resultat;

  };

} // ne pas supprimer (fin du programme)