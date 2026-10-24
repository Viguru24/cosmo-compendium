package com.example.ui.util

import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode

object AppLocalization {

    // Main App
    fun getAppTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Kochbuch"
        LanguageMode.FRENCH -> "Livre de recettes"
        LanguageMode.ITALIAN -> "Ricettario"
        LanguageMode.SPANISH -> "Recetario"
        LanguageMode.DUTCH -> "Kookboek"
        else -> "Cookbook"
    }

    fun getAppSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Formeln, Rezepte & Handwerks-Archiv"
        LanguageMode.FRENCH -> "Formules, Recettes & Archives d'Artisan"
        LanguageMode.ITALIAN -> "Formule, Ricette e Archivio Artigianale"
        LanguageMode.SPANISH -> "Fórmulas, Recetas y Archivo Artesanal"
        LanguageMode.DUTCH -> "Formules, Recepten & Handwerksarchief"
        else -> "Formulas, Recipes & Maker's Archive"
    }

    fun getSearchPlaceholder(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezepte, Zutaten suchen (Apfel, Mehl, Braten)..."
        LanguageMode.FRENCH -> "Rechercher recettes, ingrédients (pomme, farine)..."
        LanguageMode.ITALIAN -> "Cerca ricette, ingredienti (mela, farina, arrosto)..."
        LanguageMode.SPANISH -> "Buscar recetas, ingredientes (manzana, harina)..."
        LanguageMode.DUTCH -> "Zoek recepten, ingrediënten (appel, meel, braadstuk)..."
        else -> "Search recipes, ingredients (apple, flour, roast)..."
    }

    fun getScanButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezept Scannen"
        LanguageMode.FRENCH -> "Scanner Recette"
        LanguageMode.ITALIAN -> "Scansiona Ricetta"
        LanguageMode.SPANISH -> "Escanear Receta"
        LanguageMode.DUTCH -> "Recept Scannen"
        else -> "Scan Recipe"
    }

    fun getNewRecipeButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Neues Rezept"
        LanguageMode.FRENCH -> "Nouvelle Recette"
        LanguageMode.ITALIAN -> "Nuova Ricetta"
        LanguageMode.SPANISH -> "Nueva Receta"
        LanguageMode.DUTCH -> "Nieuw Recept"
        else -> "New Recipe"
    }

    fun getCategoryLabel(category: String, lang: LanguageMode = LanguageMode.ENGLISH): String {
        if (lang == LanguageMode.ENGLISH) return category
        val catLower = category.lowercase().trim()
        return when {
            catLower == "all" || catLower == "alle" -> when (lang) {
                LanguageMode.GERMAN -> "Alle"
                LanguageMode.FRENCH -> "Toutes"
                LanguageMode.ITALIAN -> "Tutte"
                LanguageMode.SPANISH -> "Todas"
                LanguageMode.DUTCH -> "Alle"
                else -> category
            }
            catLower == "baking & desserts" || catLower == "baking" || catLower == "desserts" -> when (lang) {
                LanguageMode.GERMAN -> "Backen & Desserts"
                LanguageMode.FRENCH -> "Pâtisserie & Desserts"
                LanguageMode.ITALIAN -> "Dolci e Dessert"
                LanguageMode.SPANISH -> "Repostería y Postres"
                LanguageMode.DUTCH -> "Bakken & Desserts"
                else -> category
            }
            catLower == "main dishes" || catLower == "mains" -> when (lang) {
                LanguageMode.GERMAN -> "Hauptgerichte"
                LanguageMode.FRENCH -> "Plats Principaux"
                LanguageMode.ITALIAN -> "Piatti Principali"
                LanguageMode.SPANISH -> "Platos Principales"
                LanguageMode.DUTCH -> "Hoofdgerechten"
                else -> category
            }
            catLower == "soups & stews" || catLower == "soups" || catLower == "stews" -> when (lang) {
                LanguageMode.GERMAN -> "Suppen & Eintöpfe"
                LanguageMode.FRENCH -> "Soupes & Potages"
                LanguageMode.ITALIAN -> "Zuppe e Minestre"
                LanguageMode.SPANISH -> "Sopas y Guisos"
                LanguageMode.DUTCH -> "Soepen & Stoofschotels"
                else -> category
            }
            catLower == "salads & starters" || catLower == "salads & sides" || catLower == "salads" -> when (lang) {
                LanguageMode.GERMAN -> "Salate & Vorspeisen"
                LanguageMode.FRENCH -> "Salades & Entrées"
                LanguageMode.ITALIAN -> "Insalate e Antipasti"
                LanguageMode.SPANISH -> "Ensaladas y Entrantes"
                LanguageMode.DUTCH -> "Salades & Voorgerechten"
                else -> category
            }
            catLower == "holiday & traditions" || catLower == "holiday specials" -> when (lang) {
                LanguageMode.GERMAN -> "Festtage & Traditionen"
                LanguageMode.FRENCH -> "Fêtes & Traditions"
                LanguageMode.ITALIAN -> "Feste e Tradizioni"
                LanguageMode.SPANISH -> "Fiestas y Tradiciones"
                LanguageMode.DUTCH -> "Feestdagen & Tradities"
                else -> category
            }
            catLower == "family classics" -> when (lang) {
                LanguageMode.GERMAN -> "Familien-Klassiker"
                LanguageMode.FRENCH -> "Classiques Familiaux"
                LanguageMode.ITALIAN -> "Classici di Famiglia"
                LanguageMode.SPANISH -> "Clásicos Familiares"
                LanguageMode.DUTCH -> "Familieklassiekers"
                else -> category
            }
            else -> category
        }
    }

    fun getDifficultyLabel(diff: String, lang: LanguageMode = LanguageMode.ENGLISH): String {
        val d = diff.lowercase().trim()
        return when {
            d.contains("easy") || d.contains("leicht") || d.contains("facile") || d.contains("eenvoudig") -> when (lang) {
                LanguageMode.GERMAN -> "Einfach"
                LanguageMode.FRENCH -> "Facile"
                LanguageMode.ITALIAN -> "Facile"
                LanguageMode.SPANISH -> "Fácil"
                LanguageMode.DUTCH -> "Eenvoudig"
                else -> "Easy"
            }
            d.contains("medium") || d.contains("mittel") || d.contains("moyen") || d.contains("medio") || d.contains("gemiddeld") -> when (lang) {
                LanguageMode.GERMAN -> "Mittel"
                LanguageMode.FRENCH -> "Moyen"
                LanguageMode.ITALIAN -> "Medio"
                LanguageMode.SPANISH -> "Medio"
                LanguageMode.DUTCH -> "Gemiddeld"
                else -> "Medium"
            }
            d.contains("hard") || d.contains("adv") || d.contains("schwer") || d.contains("difficile") || d.contains("difícil") || d.contains("moeilijk") -> when (lang) {
                LanguageMode.GERMAN -> "Schwer"
                LanguageMode.FRENCH -> "Difficile"
                LanguageMode.ITALIAN -> "Difficile"
                LanguageMode.SPANISH -> "Difícil"
                LanguageMode.DUTCH -> "Moeilijk"
                else -> "Advanced"
            }
            else -> diff
        }
    }

    fun normalizeServings(servings: String): String {
        if (servings.isBlank()) return "4-6 servings"
        val trimmed = servings.trim()
        val lower = trimmed.lowercase()

        // Known corrupted range and yield mappings
        val mapping = mapOf(
            "46" to "4-6 servings",
            "46 servings" to "4-6 servings",
            "810" to "8-10 servings",
            "810 servings" to "8-10 servings",
            "1012" to "10-12 servings",
            "1012 servings" to "10-12 servings",
            "68" to "6-8 servings",
            "68 servings" to "6-8 servings",
            "1618" to "16-18 servings",
            "1618 servings" to "16-18 servings",
            "3040" to "30-40 cookies",
            "3040 servings" to "30-40 cookies",
            "4125" to "4-5 jars (approx 250ml each)",
            "4125 servings" to "4-5 jars (approx 250ml each)",
            "8300" to "8 jars (approx 300g each)",
            "8300 servings" to "8 jars (approx 300g each)",
            "2200" to "2-3 jars (approx 200g each)",
            "2200 servings" to "2-3 jars (approx 200g each)",
            "90302" to "4-6 jars (approx 300g each)",
            "90302 servings" to "4-6 jars (approx 300g each)",
            "45152" to "4-5 bottles (approx 150ml each)",
            "45152 servings" to "4-5 bottles (approx 150ml each)",
            "1112" to "1-2 jars (approx 500g each)",
            "1112 servings" to "1-2 jars (approx 500g each)",
            "681" to "6-8 servings",
            "681 servings" to "6-8 servings",
            "2000" to "Serves 8-10 (2kg batch)",
            "2000 servings" to "Serves 8-10 (2kg batch)",
            "500" to "4-5 jars (approx 500g each)",
            "500 servings" to "4-5 jars (approx 500g each)",
            "200" to "Makes about 40-50 pieces",
            "200 servings" to "Makes about 40-50 pieces",
            "50" to "Makes about 50 slices",
            "50 servings" to "Makes about 50 slices"
        )

        mapping[lower]?.let { return it }

        return trimmed
    }

    fun getServingsLabel(servings: String, lang: LanguageMode = LanguageMode.ENGLISH): String {
        val normalized = normalizeServings(servings)
        if (normalized.isBlank()) return normalized

        // If it's a descriptive jar/cookie/piece label, return directly
        if (normalized.contains("jar", ignoreCase = true) ||
            normalized.contains("bottle", ignoreCase = true) ||
            normalized.contains("piece", ignoreCase = true) ||
            normalized.contains("cookie", ignoreCase = true) ||
            normalized.contains("slice", ignoreCase = true)) {
            return normalized
        }

        val clean = normalized.replace(Regex("(?i)servings?|portions?|personen|raciones|porzioni|porties"), "").trim()
        return when (lang) {
            LanguageMode.GERMAN -> if (clean.isNotBlank()) "$clean Portionen" else normalized
            LanguageMode.FRENCH -> if (clean.isNotBlank()) "$clean portions" else normalized
            LanguageMode.ITALIAN -> if (clean.isNotBlank()) "$clean porzioni" else normalized
            LanguageMode.SPANISH -> if (clean.isNotBlank()) "$clean raciones" else normalized
            LanguageMode.DUTCH -> if (clean.isNotBlank()) "$clean porties" else normalized
            else -> if (clean.isNotBlank()) "$clean servings" else normalized
        }
    }

    fun getEmptyStateTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Keine Rezepte Gefunden"
        LanguageMode.FRENCH -> "Aucune Recette Trouvée"
        LanguageMode.ITALIAN -> "Nessuna Ricetta Trovata"
        LanguageMode.SPANISH -> "No se encontraron recetas"
        LanguageMode.DUTCH -> "Geen Recepten Gevonden"
        else -> "No Recipes Found"
    }

    fun getEmptyProfileTitle(name: String, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "$name's Kochbuch ist leer"
        LanguageMode.FRENCH -> "Le livre de recettes de $name est vide"
        LanguageMode.ITALIAN -> "Il ricettario di $name è vuoto"
        LanguageMode.SPANISH -> "El recetario de $name está vacío"
        LanguageMode.DUTCH -> "$name's kookboek is leeg"
        else -> "$name's Cookbook is Empty"
    }

    fun getEmptyStateSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Beginne mit dem Scannen von Rezeptkarten, dem Importieren von Websites oder der Wiederherstellung eines Backups."
        LanguageMode.FRENCH -> "Commencez par numériser des fiches recettes, importer depuis un site web ou restaurer une sauvegarde."
        LanguageMode.ITALIAN -> "Inizia scansionando le schede di ricette, importando da siti web o ripristinando un backup."
        LanguageMode.SPANISH -> "Comienza escaneando tarjetas de recetas, importando desde sitios web o restaurando una copia de seguridad."
        LanguageMode.DUTCH -> "Begin met het scannen van receptkaarten, importeren van websites of het herstellen van een back-up."
        else -> "Start by scanning physical recipe cards, importing from a website, or restoring a backup collection."
    }

    fun getScanPhysicalCardsButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezeptkarten scannen"
        LanguageMode.FRENCH -> "Scanner fiches recettes"
        LanguageMode.ITALIAN -> "Scansiona schede ricetta"
        LanguageMode.SPANISH -> "Escanear tarjetas de receta"
        LanguageMode.DUTCH -> "Receptkaarten scannen"
        else -> "Scan Physical Recipe Cards"
    }

    fun getLoadStartersButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Klassiker laden"
        LanguageMode.FRENCH -> "Recettes de base"
        LanguageMode.ITALIAN -> "Carica classici"
        LanguageMode.SPANISH -> "Cargar recetas base"
        LanguageMode.DUTCH -> "Klassiekers laden"
        else -> "Load Starters"
    }

    fun getRestoreBackupButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Backup wiederherstellen"
        LanguageMode.FRENCH -> "Restaurer sauvegarde"
        LanguageMode.ITALIAN -> "Ripristina backup"
        LanguageMode.SPANISH -> "Restaurar copia"
        LanguageMode.DUTCH -> "Back-up herstellen"
        else -> "Restore Backup"
    }

    fun getFavoritesLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "⭐ Favoriten"
        LanguageMode.FRENCH -> "⭐ Favoris"
        LanguageMode.ITALIAN -> "⭐ Preferiti"
        LanguageMode.SPANISH -> "⭐ Favoritos"
        LanguageMode.DUTCH -> "⭐ Favorieten"
        else -> "⭐ Favorites"
    }

    fun getFilterAllFamily(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Familienmitglieder"
        LanguageMode.FRENCH -> "Toute la Famille"
        LanguageMode.ITALIAN -> "Tutta la Famiglia"
        LanguageMode.SPANISH -> "Toda la Familia"
        LanguageMode.DUTCH -> "Hele Familie"
        else -> "All Family"
    }

    fun getAddCategoryOrPerson(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "+ Kategorie / Person hinzufügen"
        LanguageMode.FRENCH -> "+ Ajouter Catégorie / Profil"
        LanguageMode.ITALIAN -> "+ Aggiungi Categoria / Profilo"
        LanguageMode.SPANISH -> "+ Añadir Categoría / Persona"
        LanguageMode.DUTCH -> "+ Categorie / Persoon toevoegen"
        else -> "+ Add Category / Person"
    }

    // Assistant Name & UI (Sous-Chef in French/English, Küchen-Assistent in German, Aiuto-Cuoco in Italian, Ayudante in Spanish, Keukenhulp in Dutch)
    fun getSousChefTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Küchen-Assistent KI"
        LanguageMode.FRENCH -> "Sous-Chef IA"
        LanguageMode.ITALIAN -> "Aiuto-Cuoco IA"
        LanguageMode.SPANISH -> "Ayudante de Cocina IA"
        LanguageMode.DUTCH -> "Keukenhulp AI"
        else -> "Sous-Chef AI Copilot"
    }

    fun getSousChefButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Küchen-Assistent"
        LanguageMode.FRENCH -> "Sous-Chef"
        LanguageMode.ITALIAN -> "Aiuto-Cuoco"
        LanguageMode.SPANISH -> "Ayudante"
        LanguageMode.DUTCH -> "Keukenhulp"
        else -> "Sous-Chef"
    }

    fun getSousChefWelcome(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wie kann ich in der Küche helfen?"
        LanguageMode.FRENCH -> "Comment puis-je vous aider en cuisine ?"
        LanguageMode.ITALIAN -> "Come posso aiutarti in cucina?"
        LanguageMode.SPANISH -> "¿Cómo puedo ayudarte en la cocina?"
        LanguageMode.DUTCH -> "Hoe kan ik helpen in de keuken?"
        else -> "How can I help in the kitchen?"
    }

    fun getSousChefSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Frag mich nach Zutaten-Alternativen, Mengenumrechnungen oder gib Sprachbefehle für deine Rezepte:"
        LanguageMode.FRENCH -> "Posez-moi des questions sur les substitutions, les conversions ou donnez des commandes vocales :"
        LanguageMode.ITALIAN -> "Chiedimi di sostituzioni di ingredienti, conversioni di unità o impartisci comandi vocali:"
        LanguageMode.SPANISH -> "Pregúntame sobre sustituciones, conversiones de unidades o da comandos de voz para tus recetas:"
        LanguageMode.DUTCH -> "Vraag me over ingrediëntvervangingen, maatomrekeningen of geef spraakopdrachten voor je recepten:"
        else -> "Ask me anything about cooking substitutions, measurements, recipe ideas, or speak natural commands:"
    }

    fun getSousChefPromptPlaceholder(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Frag den Küchen-Assistenten, 'Rezept erstellen für...' oder Text/Link einfügen..."
        LanguageMode.FRENCH -> "Demandez au Sous-Chef, 'Créer recette pour...' ou collez texte/lien..."
        LanguageMode.ITALIAN -> "Chiedi all'aiuto-cuoco, 'Crea ricetta per...' o incolla testo/link..."
        LanguageMode.SPANISH -> "Pregunta al ayudante, 'Crear receta para...' o pega texto/enlace..."
        LanguageMode.DUTCH -> "Vraag de keukenhulp, 'Recept maken voor...' of plak tekst/link..."
        else -> "Ask Sous-Chef, 'Make a recipe for...' or paste text/link..."
    }

    fun getSousChefPillCreate(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "✨ Rezept erstellen"
        LanguageMode.FRENCH -> "✨ Créer une recette"
        LanguageMode.ITALIAN -> "✨ Crea ricetta"
        LanguageMode.SPANISH -> "✨ Crear receta"
        LanguageMode.DUTCH -> "✨ Recept creëren"
        else -> "✨ Create Recipe"
    }

    fun getSousChefPillScan(name: String, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezeptkarten für $name scannen"
        LanguageMode.FRENCH -> "Scanner fiches recettes pour $name"
        LanguageMode.ITALIAN -> "Scansiona ricette per $name"
        LanguageMode.SPANISH -> "Escanear recetas para $name"
        LanguageMode.DUTCH -> "Receptkaarten voor $name scannen"
        else -> "Scan recipe cards for $name"
    }

    fun getSousChefPillImport(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezept von Web-Link importieren"
        LanguageMode.FRENCH -> "Importer une recette via lien web"
        LanguageMode.ITALIAN -> "Importa ricetta da link web"
        LanguageMode.SPANISH -> "Importar receta desde enlace web"
        LanguageMode.DUTCH -> "Recept van weblink importeren"
        else -> "Import recipe from web link"
    }

    fun getSousChefPillVideo(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezept aus Video importieren"
        LanguageMode.FRENCH -> "Importer une recette depuis une vidéo"
        LanguageMode.ITALIAN -> "Importa ricetta da video"
        LanguageMode.SPANISH -> "Importar receta de video"
        LanguageMode.DUTCH -> "Recept uit video importeren"
        else -> "Import recipe from video"
    }

    fun getSousChefPillSub(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Was kann ich statt Milch nehmen?"
        LanguageMode.FRENCH -> "Par quoi remplacer le lait ?"
        LanguageMode.ITALIAN -> "Cosa posso usare al posto del latte?"
        LanguageMode.SPANISH -> "¿Qué puedo usar en lugar de leche?"
        LanguageMode.DUTCH -> "Wat kan ik gebruiken i.p.v. melk?"
        else -> "What can I use instead of milk?"
    }

    fun getSousChefPillGrams(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wie viel Gramm hat 1 Cup Butter?"
        LanguageMode.FRENCH -> "Combien de grammes dans 1 cup de beurre ?"
        LanguageMode.ITALIAN -> "Quanti grammi ci sono in 1 cup di burro?"
        LanguageMode.SPANISH -> "¿Cuántos gramos hay en 1 taza de mantequilla?"
        LanguageMode.DUTCH -> "Hoeveel gram zit er in 1 cup boter?"
        else -> "How many grams in 1 cup of butter?"
    }

    // Profiles
    fun getProfileCookbookTitle(name: String, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "${name}s Kochbuch"
        LanguageMode.FRENCH -> "Livre de recettes de $name"
        LanguageMode.ITALIAN -> "Ricettario di $name"
        LanguageMode.SPANISH -> "Recetario de $name"
        LanguageMode.DUTCH -> "${name}'s Kookboek"
        else -> "$name's Cookbook"
    }

    fun getProfileSwitcherHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wer kocht oder kreiert heute? 👨‍🍳"
        LanguageMode.FRENCH -> "Qui crée aujourd'hui ? 👨‍🍳"
        LanguageMode.ITALIAN -> "Chi crea oggi? 👨‍🍳"
        LanguageMode.SPANISH -> "¿Quién crea hoy? 👨‍🍳"
        LanguageMode.DUTCH -> "Wie maakt er vandaag? 👨‍🍳"
        else -> "Who's Creating Today? 👨‍🍳"
    }

    fun getProfileSwitcherSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wähle das persönliche Kochbuch eines Mitglieds"
        LanguageMode.FRENCH -> "Choisissez le livre de recettes personnel d'un membre"
        LanguageMode.ITALIAN -> "Scegli il ricettario personale di un membro"
        LanguageMode.SPANISH -> "Elige el recetario personal de un miembro"
        LanguageMode.DUTCH -> "Kies het persoonlijke kookboek van een lid"
        else -> "Switch to a member's personal cookbook"
    }

    fun getAddProfileButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "+ Mitglied hinzufügen"
        LanguageMode.FRENCH -> "+ Ajouter Membre"
        LanguageMode.ITALIAN -> "+ Aggiungi Membro"
        LanguageMode.SPANISH -> "+ Añadir Miembro"
        LanguageMode.DUTCH -> "+ Lid toevoegen"
        else -> "+ Add Member"
    }

    fun getProfileSavedCount(count: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "$count gespeicherte Rezepte"
        LanguageMode.FRENCH -> "$count recettes enregistrées"
        LanguageMode.ITALIAN -> "$count ricette salvate"
        LanguageMode.SPANISH -> "$count recetas guardadas"
        LanguageMode.DUTCH -> "$count opgeslagen recepten"
        else -> "$count saved recipes"
    }

    fun getBulkMoveButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezepte stapelweise verschieben"
        LanguageMode.FRENCH -> "Déplacer des recettes en masse"
        LanguageMode.ITALIAN -> "Sposta ricette in blocco"
        LanguageMode.SPANISH -> "Mover recetas por lotes"
        LanguageMode.DUTCH -> "Recepten in bulk verplaatsen"
        else -> "Bulk Move Recipes"
    }

    fun getBulkMoveDescription(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezepte mit 1 Klick zwischen Profilen übertragen"
        LanguageMode.FRENCH -> "Transférez des recettes entre profils en 1 clic"
        LanguageMode.ITALIAN -> "Trasferisci ricette tra profili con 1 clic"
        LanguageMode.SPANISH -> "Transfiere recetas entre perfiles con 1 clic"
        LanguageMode.DUTCH -> "Verplaats recepten met 1 klik tussen profielen"
        else -> "Transfer recipes between member books in 1 click"
    }

    fun getAllFamilyProfileHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Familienrezepte"
        LanguageMode.FRENCH -> "Toutes les Recettes Familiales"
        LanguageMode.ITALIAN -> "Tutte le Ricette di Famiglia"
        LanguageMode.SPANISH -> "Todas las Recetas Familiares"
        LanguageMode.DUTCH -> "Alle Familierecepten"
        else -> "All Family Recipes"
    }

    // Booklet Page Tabs & Spreads
    fun getTabCover(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "📖 Einband & Geschichte"
        LanguageMode.FRENCH -> "📖 Couverture & Histoire"
        LanguageMode.ITALIAN -> "📖 Copertina e Storia"
        LanguageMode.SPANISH -> "📖 Portada e Historia"
        LanguageMode.DUTCH -> "📖 Omslag & Verhaal"
        else -> "📖 Cover & Lore"
    }

    fun getTabIngredients(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "🥗 Zutaten & Schritte"
        LanguageMode.FRENCH -> "🥗 Ingrédients & Étapes"
        LanguageMode.ITALIAN -> "🥗 Ingredienti e Passaggi"
        LanguageMode.SPANISH -> "🥗 Ingredientes y Pasos"
        LanguageMode.DUTCH -> "🥗 Ingrediënten & Stappen"
        else -> "🥗 Ingredients & Steps"
    }

    fun getTabJournal(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "✍️ Koch-Tagebuch"
        LanguageMode.FRENCH -> "✍️ Journal du Cuisinier"
        LanguageMode.ITALIAN -> "✍️ Diario del Cuoco"
        LanguageMode.SPANISH -> "✍️ Diario del Cocinero"
        LanguageMode.DUTCH -> "✍️ Kookdagboek"
        else -> "✍️ Cook's Journal"
    }

    fun getCoverHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "REZEPTSAMMLUNG"
        LanguageMode.FRENCH -> "RECUEIL DE RECETTES"
        LanguageMode.ITALIAN -> "RACCOLTA DI RICETTE"
        LanguageMode.SPANISH -> "COLECCIÓN DE RECETAS"
        LanguageMode.DUTCH -> "RECEPTENVERZAMELING"
        else -> "RECIPE COLLECTION"
    }

    fun getCoverLore(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Bewahrtes Familienerbe & Küchengeheimnisse"
        LanguageMode.FRENCH -> "Héritage Familial & Secrets Culinaires"
        LanguageMode.ITALIAN -> "Patrimonio di Famiglia e Segreti di Cucina"
        LanguageMode.SPANISH -> "Herencia Familiar y Secretos de Cocina"
        LanguageMode.DUTCH -> "Gekoesterd Familie-erfgoed & Keukengeheimen"
        else -> "Preserved Family Heritage & Kitchen Secrets"
    }

    fun getGenerateAiCoverButton(hasPhoto: Boolean, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> if (hasPhoto) "✨ KI-Foto neu generieren" else "✨ KI-Titelbild generieren"
        LanguageMode.FRENCH -> if (hasPhoto) "✨ Régénérer Photo IA" else "✨ Générer Couverture IA"
        LanguageMode.ITALIAN -> if (hasPhoto) "✨ Rigenera Foto IA" else "✨ Genera Copertina IA"
        LanguageMode.SPANISH -> if (hasPhoto) "✨ Regenerar Foto con IA" else "✨ Generar Portada con IA"
        LanguageMode.DUTCH -> if (hasPhoto) "✨ AI-Foto opnieuw genereren" else "✨ AI-Omslagfoto genereren"
        else -> if (hasPhoto) "✨ Regenerate AI Photo" else "✨ Generate AI Cover Photo"
    }

    fun getCreatingAiCoverStatus(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Erstelle KI-Food-Foto..."
        LanguageMode.FRENCH -> "Création de la Photo IA..."
        LanguageMode.ITALIAN -> "Creazione Foto IA..."
        LanguageMode.SPANISH -> "Creando Foto con IA..."
        LanguageMode.DUTCH -> "AI-Foodfoto maken..."
        else -> "Creating AI Food Photo..."
    }

    fun getSwipeOrTapToOpen(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Zum Öffnen wischen oder tippen"
        LanguageMode.FRENCH -> "Balayez ou touchez pour ouvrir"
        LanguageMode.ITALIAN -> "Scorri o tocca per aprire"
        LanguageMode.SPANISH -> "Desliza o toca para abrir"
        LanguageMode.DUTCH -> "Veeg of tik om te openen"
        else -> "Swipe or tap to open"
    }

    fun getTurnPageLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Umblättern"
        LanguageMode.FRENCH -> "Tourner Page"
        LanguageMode.ITALIAN -> "Gira Pagina"
        LanguageMode.SPANISH -> "Pasar Página"
        LanguageMode.DUTCH -> "Omslaan"
        else -> "Turn Page"
    }

    fun getTurnBackLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Zurückblättern"
        LanguageMode.FRENCH -> "Page Précédente"
        LanguageMode.ITALIAN -> "Pagina Precedente"
        LanguageMode.SPANISH -> "Página Anterior"
        LanguageMode.DUTCH -> "Terugslaan"
        else -> "Turn Back"
    }

    // Cook Mode
    fun getCookModeTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Küchen-Kochmodus"
        LanguageMode.FRENCH -> "Mode Cuisson Mains-Libres"
        LanguageMode.ITALIAN -> "Modalità Cucina"
        LanguageMode.SPANISH -> "Modo Cocina"
        LanguageMode.DUTCH -> "Kookmodus"
        else -> "Kitchen Cooking Mode"
    }

    fun getCookModeStepHeader(current: Int, total: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schritt $current von $total"
        LanguageMode.FRENCH -> "Étape $current sur $total"
        LanguageMode.ITALIAN -> "Passaggio $current di $total"
        LanguageMode.SPANISH -> "Paso $current de $total"
        LanguageMode.DUTCH -> "Stap $current van $total"
        else -> "Step $current of $total"
    }

    fun getMarkDoneLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schritt als erledigt markieren"
        LanguageMode.FRENCH -> "Marquer l'étape comme terminée"
        LanguageMode.ITALIAN -> "Segna come completato"
        LanguageMode.SPANISH -> "Marcar paso como completado"
        LanguageMode.DUTCH -> "Markeer stap als voltooid"
        else -> "Mark Step as Done"
    }

    fun getStartCookingButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Kochmodus starten"
        LanguageMode.FRENCH -> "Démarrer Mode Cuisson"
        LanguageMode.ITALIAN -> "Avvia Cottura"
        LanguageMode.SPANISH -> "Iniciar Modo Cocina"
        LanguageMode.DUTCH -> "Start Kookmodus"
        else -> "Start Cooking Mode"
    }

    // Shopping List
    fun getShoppingListTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Einkaufsliste"
        LanguageMode.FRENCH -> "Liste de Courses"
        LanguageMode.ITALIAN -> "Lista della Spesa"
        LanguageMode.SPANISH -> "Lista de Compras"
        LanguageMode.DUTCH -> "Boodschappenlijst"
        else -> "Shopping List"
    }

    fun getAddAllToShoppingList(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Zur Einkaufsliste Hinzufügen"
        LanguageMode.FRENCH -> "Ajouter à la Liste de Courses"
        LanguageMode.ITALIAN -> "Aggiungi alla Lista Spesa"
        LanguageMode.SPANISH -> "Añadir a la Lista de Compras"
        LanguageMode.DUTCH -> "Toevoegen aan Boodschappenlijst"
        else -> "Add All to Shopping List"
    }

    // Settings Headers & Labels

    // ==========================================
    // SETTINGS & PREFERENCES FORWARDERS
    // ==========================================
    fun getSettingsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSettingsTitle(lang)
    fun getSettingsSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSettingsSubtitle(lang)
    fun getLanguageSectionTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getLanguageSectionTitle(lang)
    fun getLanguageSectionSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getLanguageSectionSubtitle(lang)
    fun getCollectionOverviewTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCollectionOverviewTitle(lang)
    fun getBatchGeneratePhotosButton(count: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getBatchGeneratePhotosButton(count, lang)
    fun getCloudSyncTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCloudSyncTitle(lang)
    fun getCloudSyncActiveBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCloudSyncActiveBadge(lang)
    fun getCloudSyncDisabledBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCloudSyncDisabledBadge(lang)
    fun getCloudSyncDescription(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCloudSyncDescription(lang)
    fun getServerUrlLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getServerUrlLabel(lang)
    fun getSecretTokenLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSecretTokenLabel(lang)
    fun getTestConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getTestConnectionButton(lang)
    fun getTestingConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getTestingConnectionButton(lang)
    fun getSyncNowButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSyncNowButton(lang)
    fun getAutoSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getAutoSyncLabel(lang)
    fun getImmediateSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getImmediateSyncLabel(lang)
    fun getAutoWeeklyBackupTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getAutoWeeklyBackupTitle(lang)
    fun getAutoWeeklyBackupBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getAutoWeeklyBackupBadge(lang)
    fun getAutoWeeklyBackupDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getAutoWeeklyBackupDesc(lang)
    fun getOpenBackupManagerButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getOpenBackupManagerButton(lang)
    fun getGeminiTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiTitle(lang)
    fun getGeminiSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiSubtitle(lang)
    fun getGeminiDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiDesc(lang)
    fun getGeminiKeyLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiKeyLabel(lang)
    fun getGeminiTestButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiTestButton(lang)
    fun getGeminiKeyHelp(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getGeminiKeyHelp(lang)
    fun getAiPhotoEngineSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getAiPhotoEngineSubtitle(lang)
    fun getCookingAndAppPrefsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCookingAndAppPrefsTitle(lang)
    fun getKeepScreenAwakeTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getKeepScreenAwakeTitle(lang)
    fun getKeepScreenAwakeDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getKeepScreenAwakeDesc(lang)
    fun getSoundEffectsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSoundEffectsTitle(lang)
    fun getSoundEffectsDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSoundEffectsDesc(lang)
    fun getUnitSystemTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getUnitSystemTitle(lang)
    fun getUnitSystemDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getUnitSystemDesc(lang)
    fun getDangerZoneTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDangerZoneTitle(lang)
    fun getDangerZoneDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDangerZoneDesc(lang)
    fun getDoneButton(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDoneButton(lang)
    fun getEditButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getEditButtonLabel(lang)
    fun getShareButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getShareButtonLabel(lang)
    fun getDeleteAllTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDeleteAllTitle(lang)
    fun getDeleteAllConfirmDialogTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDeleteAllConfirmDialogTitle(lang)
    fun getDeleteAllConfirmDialogMessage(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getDeleteAllConfirmDialogMessage(lang)
    fun getCloseLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCloseLabel(lang)
    fun getCancelLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getCancelLabel(lang)
    fun getSaveLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = AppLocalizationSettings.getSaveLabel(lang)

}


fun RecipeEntity.getDisplayTitle(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return when (lang) {
        LanguageMode.GERMAN -> if (titleGerman.isNotBlank()) titleGerman else title
        LanguageMode.ENGLISH -> if (titleEnglish.isNotBlank()) titleEnglish else title
        else -> if (titleEnglish.isNotBlank()) titleEnglish else title
    }
}

fun RecipeEntity.getDisplayCategory(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return AppLocalization.getCategoryLabel(category, lang)
}
