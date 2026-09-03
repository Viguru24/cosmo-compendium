package com.example.ui.util

import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode

object AppLocalization {

    // Main App
    fun getAppTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Kompendium"
        LanguageMode.FRENCH -> "Compendium"
        LanguageMode.ITALIAN -> "Compendio"
        LanguageMode.SPANISH -> "Compendio"
        LanguageMode.DUTCH -> "Compendium"
        else -> "Compendium"
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

    fun getServingsLabel(servings: String, lang: LanguageMode = LanguageMode.ENGLISH): String {
        if (servings.isBlank()) return servings
        val clean = servings.replace(Regex("(?i)servings?|portions?|personen|raciones|porzioni|porties"), "").trim()
        return when (lang) {
            LanguageMode.GERMAN -> if (clean.isNotBlank()) "$clean Portionen" else servings
            LanguageMode.FRENCH -> if (clean.isNotBlank()) "$clean portions" else servings
            LanguageMode.ITALIAN -> if (clean.isNotBlank()) "$clean porzioni" else servings
            LanguageMode.SPANISH -> if (clean.isNotBlank()) "$clean raciones" else servings
            LanguageMode.DUTCH -> if (clean.isNotBlank()) "$clean porties" else servings
            else -> if (clean.isNotBlank()) "$clean servings" else servings
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
        LanguageMode.GERMAN -> "$name's Kompendium ist leer"
        LanguageMode.FRENCH -> "Le compendium de $name est vide"
        LanguageMode.ITALIAN -> "Il compendio di $name è vuoto"
        LanguageMode.SPANISH -> "El compendio de $name está vacío"
        LanguageMode.DUTCH -> "$name's compendium is leeg"
        else -> "$name's Compendium is Empty"
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
        LanguageMode.GERMAN -> "Frag den Küchen-Assistenten..."
        LanguageMode.FRENCH -> "Demandez au Sous-Chef..."
        LanguageMode.ITALIAN -> "Chiedi all'aiuto-cuoco..."
        LanguageMode.SPANISH -> "Pregunta al ayudante..."
        LanguageMode.DUTCH -> "Vraag de keukenhulp..."
        else -> "Ask Sous-Chef or paste link..."
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
        LanguageMode.GERMAN -> "${name}s Kompendium"
        LanguageMode.FRENCH -> "Compendium de $name"
        LanguageMode.ITALIAN -> "Compendio di $name"
        LanguageMode.SPANISH -> "Compendio de $name"
        LanguageMode.DUTCH -> "${name}'s Compendium"
        else -> "$name's Compendium"
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
        LanguageMode.GERMAN -> "Wähle das persönliche Kompendium eines Mitglieds"
        LanguageMode.FRENCH -> "Choisissez le compendium personnel d'un membre"
        LanguageMode.ITALIAN -> "Scegli il compendio personale di un membro"
        LanguageMode.SPANISH -> "Elige el compendio personal de un miembro"
        LanguageMode.DUTCH -> "Kies het persoonlijke compendium van een lid"
        else -> "Switch to a member's personal compendium"
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
    fun getSettingsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Einstellungen & Cloud Hub"
        LanguageMode.FRENCH -> "Paramètres & Cloud Hub"
        LanguageMode.ITALIAN -> "Impostazioni e Cloud Hub"
        LanguageMode.SPANISH -> "Ajustes y Cloud Hub"
        LanguageMode.DUTCH -> "Instellingen & Cloud Hub"
        else -> "Settings & Cloud Hub"
    }

    fun getSettingsSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Synchronisation, Backups, KI-Fotografie & Präferenzen verwalten"
        LanguageMode.FRENCH -> "Gérer la synchronisation, les sauvegardes et la génération IA"
        LanguageMode.ITALIAN -> "Gestisci sincronizzazione, backup e generazione foto IA"
        LanguageMode.SPANISH -> "Gestionar sincronización, copias de seguridad y fotos con IA"
        LanguageMode.DUTCH -> "Beheer synchronisatie, back-ups en AI-fotogeneratie"
        else -> "Manage sync, backups, AI photo generation & preferences"
    }

    fun getLanguageSectionTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "App-Sprache & Übersetzung"
        LanguageMode.FRENCH -> "Langue de l'Application"
        LanguageMode.ITALIAN -> "Lingua dell'Applicazione"
        LanguageMode.SPANISH -> "Idioma de la Aplicación"
        LanguageMode.DUTCH -> "Applicatietaal"
        else -> "App Display Language"
    }

    fun getLanguageSectionSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Menüs, Buttons, Einstellungen und Rezepte in Echtzeit anpassen"
        LanguageMode.FRENCH -> "Changer instantanément tous les menus, boutons et recettes"
        LanguageMode.ITALIAN -> "Cambia istantaneamente menu, pulsanti e ricette"
        LanguageMode.SPANISH -> "Cambiar menús, botones y recetas al instante"
        LanguageMode.DUTCH -> "Pas menu's, knoppen en recepten direct aan"
        else -> "Switch UI words, tabs, and recipe titles"
    }

    fun getCollectionOverviewTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Kompendium-Sammlung Übersicht"
        LanguageMode.FRENCH -> "Aperçu de la Collection du Compendium"
        LanguageMode.ITALIAN -> "Panoramica della Collezione del Compendio"
        LanguageMode.SPANISH -> "Resumen de la Colección del Compendio"
        LanguageMode.DUTCH -> "Overzicht Compendiumcollectie"
        else -> "Compendium Collection Overview"
    }

    fun getBatchGeneratePhotosButton(count: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "KI-Fotos im Stapel generieren ($count bereit)"
        LanguageMode.FRENCH -> "Générer les photos IA en lot ($count prêtes)"
        LanguageMode.ITALIAN -> "Genera foto IA in blocco ($count pronte)"
        LanguageMode.SPANISH -> "Generar fotos por lotes con IA ($count listas)"
        LanguageMode.DUTCH -> "AI-foto's in bulk genereren ($count gereed)"
        else -> "Batch Generate AI Photos ($count ready)"
    }

    fun getCloudSyncTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Cloud Hub & Multi-Geräte Sync"
        LanguageMode.FRENCH -> "Cloud Hub & Synchronisation Multi-Appareils"
        LanguageMode.ITALIAN -> "Cloud Hub & Sincronizzazione Multi-Dispositivo"
        LanguageMode.SPANISH -> "Cloud Hub y Sincronización Multi-Dispositivo"
        LanguageMode.DUTCH -> "Cloud Hub & Multi-Apparaat Synchronisatie"
        else -> "Cloud Hub & Multi-Device Sync"
    }

    fun getCloudSyncActiveBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Aktiv • Verbunden mit privatem VPS"
        LanguageMode.FRENCH -> "Actif • Connecté au VPS privé"
        LanguageMode.ITALIAN -> "Attivo • Connesso al VPS privato"
        LanguageMode.SPANISH -> "Activo • Conectado al VPS privado"
        LanguageMode.DUTCH -> "Actief • Verbonden met privé VPS"
        else -> "Active • Connected to Private VPS"
    }

    fun getCloudSyncDisabledBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Deaktiviert • Nur lokaler Speicher"
        LanguageMode.FRENCH -> "Désactivé • Stockage local uniquement"
        LanguageMode.ITALIAN -> "Disattivato • Solo archiviazione locale"
        LanguageMode.SPANISH -> "Desactivado • Solo almacenamiento local"
        LanguageMode.DUTCH -> "Uitgeschakeld • Alleen lokale opslag"
        else -> "Disabled • Local storage only"
    }

    fun getCloudSyncDescription(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Synchronisiere Kompendien, Rezepte, Formeln & Fotos automatisch über dein privates Cloud-VPS oder Nextcloud-Konto."
        LanguageMode.FRENCH -> "Synchronisez automatiquement vos compendiums, formules et photos sur votre serveur privé."
        LanguageMode.ITALIAN -> "Sincronizza automaticamente compendi, formule e foto tramite il tuo server privato."
        LanguageMode.SPANISH -> "Sincroniza automáticamente compendios, fórmulas y fotos a través de tu servidor privado."
        LanguageMode.DUTCH -> "Synchroniseer compendiums, formules en foto's automatisch via je privéserver."
        else -> "Auto-sync compendiums, formulas & photos across family tablets via your private VPS or Nextcloud server."
    }

    fun getServerUrlLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Server URL / Endpunkt"
        LanguageMode.FRENCH -> "URL du Serveur / Point d'accès"
        LanguageMode.ITALIAN -> "URL Server / Endpoint"
        LanguageMode.SPANISH -> "URL del Servidor / Punto de acceso"
        LanguageMode.DUTCH -> "Server-URL / Eindpunt"
        else -> "Server URL / Endpoint"
    }

    fun getSecretTokenLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Geheimer Synchronisations-Token"
        LanguageMode.FRENCH -> "Jeton de Synchronisation Secret"
        LanguageMode.ITALIAN -> "Token di Sincronizzazione Segreto"
        LanguageMode.SPANISH -> "Token de Sincronización Secreto"
        LanguageMode.DUTCH -> "Geheime Synchronisatie-token"
        else -> "Secret Sync Token"
    }

    fun getTestConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verbindung testen"
        LanguageMode.FRENCH -> "Tester Connexion"
        LanguageMode.ITALIAN -> "Verifica Connessione"
        LanguageMode.SPANISH -> "Probar Conexión"
        LanguageMode.DUTCH -> "Verbinding testen"
        else -> "Test Connection"
    }

    fun getTestingConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verbindung wird geprüft..."
        LanguageMode.FRENCH -> "Vérification de la connexion..."
        LanguageMode.ITALIAN -> "Verifica della connessione..."
        LanguageMode.SPANISH -> "Verificando conexión..."
        LanguageMode.DUTCH -> "Verbinding controleren..."
        else -> "Testing Connection..."
    }

    fun getSyncNowButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Jetzt synchronisieren"
        LanguageMode.FRENCH -> "Synchroniser maintenant"
        LanguageMode.ITALIAN -> "Sincronizza ora"
        LanguageMode.SPANISH -> "Sincronizar ahora"
        LanguageMode.DUTCH -> "Nu synchroniseren"
        else -> "Sync Now"
    }

    fun getAutoSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Automatische Hintergrund-Synchronisation"
        LanguageMode.FRENCH -> "Synchronisation automatique en arrière-plan"
        LanguageMode.ITALIAN -> "Sincronizzazione automatica in background"
        LanguageMode.SPANISH -> "Sincronización automática en segundo plano"
        LanguageMode.DUTCH -> "Automatische achtergrondsycnhronisatie"
        else -> "Automatic background sync"
    }

    fun getImmediateSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Beim Speichern sofort hochladen"
        LanguageMode.FRENCH -> "Envoyer immédiatement lors de l'enregistrement"
        LanguageMode.ITALIAN -> "Carica immediatamente al salvataggio"
        LanguageMode.SPANISH -> "Subir inmediatamente al guardar"
        LanguageMode.DUTCH -> "Direct uploaden bij opslaan"
        else -> "Upload immediately upon saving"
    }

    fun getAutoWeeklyBackupTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Automatische wöchentliche Sicherung"
        LanguageMode.FRENCH -> "Sauvegardes Automatiques Hebdomadaires"
        LanguageMode.ITALIAN -> "Backup Settimanali Automatici"
        LanguageMode.SPANISH -> "Copias de Seguridad Semanales Automáticas"
        LanguageMode.DUTCH -> "Automatische Wekelijkse Back-ups"
        else -> "Weekly Automated Backups"
    }

    fun getAutoWeeklyBackupBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Aktiviert • Sichert alle 7 Tage automatisch"
        LanguageMode.FRENCH -> "Activé • Sauvegarde auto tous les 7 jours"
        LanguageMode.ITALIAN -> "Attivato • Backup auto ogni 7 giorni"
        LanguageMode.SPANISH -> "Activado • Copia auto cada 7 días"
        LanguageMode.DUTCH -> "Ingeschakeld • Back-up auto elke 7 dagen"
        else -> "Enabled • Backs up automatically every 7 days"
    }

    fun getAutoWeeklyBackupDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Erstellt jeden Sonntag automatisch ein vollständiges ZIP-Archiv deiner Rezeptdatenbank und speichert es lokal auf diesem Gerät."
        LanguageMode.FRENCH -> "Crée automatiquement chaque dimanche une archive ZIP complète de vos recettes sur cet appareil."
        LanguageMode.ITALIAN -> "Crea automaticamente ogni domenica un archivio ZIP completo delle tue ricette su questo dispositivo."
        LanguageMode.SPANISH -> "Crea automáticamente cada domingo un archivo ZIP completo de tus recetas en este dispositivo."
        LanguageMode.DUTCH -> "Maakt elke zondag automatisch een compleet ZIP-archief van je recepten op dit apparaat."
        else -> "Creates a full ZIP snapshot archive of your compendium database every Sunday and keeps it saved on this device."
    }

    fun getOpenBackupManagerButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Datensicherungs- & Wiederherstellungs-Tool öffnen"
        LanguageMode.FRENCH -> "Ouvrir l'outil de sauvegarde & restauration"
        LanguageMode.ITALIAN -> "Apri strumento di backup e ripristino"
        LanguageMode.SPANISH -> "Abrir herramienta de copia y restauración"
        LanguageMode.DUTCH -> "Back-up- & hersteltool openen"
        else -> "Open Backup & Restore Tool"
    }

    fun getGeminiTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Google Gemini & Küchen-Assistent KI"
        LanguageMode.FRENCH -> "Google Gemini & Sous-Chef IA"
        LanguageMode.ITALIAN -> "Google Gemini e Aiuto-Cuoco IA"
        LanguageMode.SPANISH -> "Google Gemini y Ayudante de Cocina IA"
        LanguageMode.DUTCH -> "Google Gemini & Keukenhulp AI"
        else -> "Google Gemini & Sous-Chef AI"
    }

    fun getGeminiSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schlüssel erforderlich für OCR-Kartenscan & Sprachassistent"
        LanguageMode.FRENCH -> "Clé requise pour la numérisation OCR et l'assistant vocal"
        LanguageMode.ITALIAN -> "Chiave richiesta per scansione OCR e assistente vocale"
        LanguageMode.SPANISH -> "Clave requerida para escaneo OCR y asistente de voz"
        LanguageMode.DUTCH -> "Sleutel vereist voor OCR-kaartscan & spraakassistent"
        else -> "Key required for card OCR & voice assistant"
    }

    fun getGeminiDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Für das automatische Auslesen handgeschriebener Rezeptkarten, Mengenumrechnungen und KI-Sprachbefehle."
        LanguageMode.FRENCH -> "Pour transcrire les fiches manuscrites, convertir les unités et exécuter les commandes vocales."
        LanguageMode.ITALIAN -> "Per trascrivere schede manoscritte, convertire unità ed eseguire comandi vocali."
        LanguageMode.SPANISH -> "Para transcribir tarjetas manuscritas, convertir unidades y ejecutar comandos de voz."
        LanguageMode.DUTCH -> "Voor het transcriberen van handgeschreven kaarten, maatomrekening en spraakopdrachten."
        else -> "Powers multi-page card vision scans, ingredient extraction, and voice assistant features."
    }

    fun getGeminiKeyLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Google Gemini API-Schlüssel"
        LanguageMode.FRENCH -> "Clé API Google Gemini"
        LanguageMode.ITALIAN -> "Chiave API Google Gemini"
        LanguageMode.SPANISH -> "Clave API de Google Gemini"
        LanguageMode.DUTCH -> "Google Gemini API-sleutel"
        else -> "Google Gemini API Key"
    }

    fun getGeminiTestButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Gemini API Verbindung testen"
        LanguageMode.FRENCH -> "Tester la connexion API Gemini"
        LanguageMode.ITALIAN -> "Verifica connessione API Gemini"
        LanguageMode.SPANISH -> "Probar conexión con API de Gemini"
        LanguageMode.DUTCH -> "Gemini API-verbinding testen"
        else -> "Test Gemini API Connection"
    }

    fun getGeminiKeyHelp(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "💡 Kostenlosen API-Schlüssel erstellen auf aistudio.google.com/apikey"
        LanguageMode.FRENCH -> "💡 Obtenez une clé API gratuite sur aistudio.google.com/apikey"
        LanguageMode.ITALIAN -> "💡 Ottieni una chiave API gratuita su aistudio.google.com/apikey"
        LanguageMode.SPANISH -> "💡 Obtén una clave API gratuita en aistudio.google.com/apikey"
        LanguageMode.DUTCH -> "💡 Krijg een gratis API-sleutel op aistudio.google.com/apikey"
        else -> "💡 Get a free API key at aistudio.google.com/apikey"
    }

    fun getAiPhotoEngineSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wähle Cloud Gemini KI oder lokales ComfyUI auf deinem PC"
        LanguageMode.FRENCH -> "Choisissez Gemini Cloud IA ou ComfyUI sur PC local"
        LanguageMode.ITALIAN -> "Scegli Gemini Cloud IA o ComfyUI su PC locale"
        LanguageMode.SPANISH -> "Elige Gemini Cloud IA o ComfyUI en PC local"
        LanguageMode.DUTCH -> "Kies Cloud Gemini AI of lokale PC ComfyUI"
        else -> "Choose Cloud Gemini AI or your local PC's ComfyUI"
    }

    fun getCookingAndAppPrefsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Koch- & App-Einstellungen"
        LanguageMode.FRENCH -> "Préférences de Cuisson & App"
        LanguageMode.ITALIAN -> "Preferenze di Cucina e App"
        LanguageMode.SPANISH -> "Preferencias de Cocina y App"
        LanguageMode.DUTCH -> "Kook- & App-instellingen"
        else -> "Cooking & App Preferences"
    }

    fun getKeepScreenAwakeTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Bildschirm im Kochmodus aktiv halten"
        LanguageMode.FRENCH -> "Garder l'écran allumé en mode cuisson"
        LanguageMode.ITALIAN -> "Mantieni schermo attivo in modalità cucina"
        LanguageMode.SPANISH -> "Mantener pantalla encendida en modo cocina"
        LanguageMode.DUTCH -> "Scherm aanhouden tijdens koken"
        else -> "Keep Screen Awake in Cook Mode"
    }

    fun getKeepScreenAwakeDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verhindert Display-Abschaltung beim Kochen in der Küche"
        LanguageMode.FRENCH -> "Empêche l'écran de s'éteindre pendant la cuisine"
        LanguageMode.ITALIAN -> "Impedisce lo spegnimento dello schermo durante la preparazione"
        LanguageMode.SPANISH -> "Evita que la pantalla se apague mientras cocinas"
        LanguageMode.DUTCH -> "Voorkomt time-out van het scherm tijdens het koken"
        else -> "Prevents display timeout while cooking in the kitchen"
    }

    fun getSoundEffectsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Soundeffekte & Umblättern"
        LanguageMode.FRENCH -> "Effets Sonores & Audio"
        LanguageMode.ITALIAN -> "Effetti Sonori e Audio"
        LanguageMode.SPANISH -> "Efectos de Sonido y Audio"
        LanguageMode.DUTCH -> "Geluidseffecten & Audio"
        else -> "Sound Effects & Page Turns"
    }

    fun getSoundEffectsDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Sanfte Blätter-Sounds und Timer-Signaltöne"
        LanguageMode.FRENCH -> "Sons tactiles de feuilletage et minuteries"
        LanguageMode.ITALIAN -> "Suoni tattili di sfogliamento e suonerie timer"
        LanguageMode.SPANISH -> "Sonidos de cambio de página y alarmas de temporizador"
        LanguageMode.DUTCH -> "Voelbare pagina-omslaggeluiden en timersignalen"
        else -> "Tactile page flip sounds and timer chimes"
    }

    fun getUnitSystemTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Standard-Maßsystem & Einheiten"
        LanguageMode.FRENCH -> "Système de Mesure par Défaut"
        LanguageMode.ITALIAN -> "Sistema di Misura Preferito"
        LanguageMode.SPANISH -> "Sistema de Medidas Preferido"
        LanguageMode.DUTCH -> "Voorkeurs Maateenheden"
        else -> "Default Measuring Style & Unit System"
    }

    fun getUnitSystemDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Einheiten können auch auf jeder Rezeptkarte einzeln umgeschaltet werden"
        LanguageMode.FRENCH -> "Vous pouvez aussi changer les unités sur chaque fiche recette"
        LanguageMode.ITALIAN -> "Puoi anche cambiare le unità su ogni singola scheda ricetta"
        LanguageMode.SPANISH -> "También puedes cambiar unidades en cada tarjeta de receta"
        LanguageMode.DUTCH -> "Je kunt maateenheden ook op elke receptkaart afzonderlijk wijzigen"
        else -> "You can also switch units on any individual recipe card"
    }

    fun getDangerZoneTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Gefahrenzone"
        LanguageMode.FRENCH -> "Zone de Danger"
        LanguageMode.ITALIAN -> "Zona di Pericolo"
        LanguageMode.SPANISH -> "Zona de Peligro"
        LanguageMode.DUTCH -> "Gevarenzone"
        else -> "Danger Zone"
    }

    fun getDangerZoneDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Rezepte und Bilder unwiderruflich von diesem Gerät entfernen"
        LanguageMode.FRENCH -> "Supprimer irréversiblement toutes les recettes de cet appareil"
        LanguageMode.ITALIAN -> "Elimina definitivamente tutte le ricette da questo dispositivo"
        LanguageMode.SPANISH -> "Eliminar irreversiblemente todas las recetas de este dispositivo"
        LanguageMode.DUTCH -> "Verwijder alle recepten en afbeeldingen definitief van dit apparaat"
        else -> "Permanently remove all recipes and local data from this device"
    }

    fun getDoneButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Fertig"
        LanguageMode.FRENCH -> "Terminé"
        LanguageMode.ITALIAN -> "Fatto"
        LanguageMode.SPANISH -> "Listo"
        LanguageMode.DUTCH -> "Klaar"
        else -> "Done"
    }

    fun getEditButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Bearbeiten"
        LanguageMode.FRENCH -> "Modifier"
        LanguageMode.ITALIAN -> "Modifica"
        LanguageMode.SPANISH -> "Editar"
        LanguageMode.DUTCH -> "Bewerken"
        else -> "Edit"
    }

    fun getShareButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Teilen"
        LanguageMode.FRENCH -> "Partager"
        LanguageMode.ITALIAN -> "Condividi"
        LanguageMode.SPANISH -> "Compartir"
        LanguageMode.DUTCH -> "Delen"
        else -> "Share"
    }

    fun getDeleteAllTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Rezepte löschen"
        LanguageMode.FRENCH -> "Supprimer toutes les recettes"
        LanguageMode.ITALIAN -> "Elimina tutte le ricette"
        LanguageMode.SPANISH -> "Eliminar todas las recetas"
        LanguageMode.DUTCH -> "Verwijder alle recepten"
        else -> "Delete All Recipes"
    }

    fun getDeleteAllConfirmDialogTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wirklich alle Rezepte löschen?"
        LanguageMode.FRENCH -> "Vraiment supprimer toutes les recettes ?"
        LanguageMode.ITALIAN -> "Vuoi davvero eliminare tutte le ricette?"
        LanguageMode.SPANISH -> "¿Realmente deseas eliminar todas las recetas?"
        LanguageMode.DUTCH -> "Weet je zeker dat je alle recepten wilt verwijderen?"
        else -> "Delete all recipes permanently?"
    }

    fun getDeleteAllConfirmDialogMessage(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Diese Aktion kann nicht rückgängig gemacht werden. Alle Rezepte, Fotos und Notizen werden gelöscht."
        LanguageMode.FRENCH -> "Cette action est irréversible. Toutes les recettes, photos et notes seront effacées."
        LanguageMode.ITALIAN -> "Questa azione è irreversibile. Tutte le ricette, foto e note verranno eliminate."
        LanguageMode.SPANISH -> "Esta acción es irreversible. Todas las recetas, fotos y notas serán eliminadas."
        LanguageMode.DUTCH -> "Deze actie kan niet ongedaan worden gemaakt. Alle recepten, foto's en notities worden gewist."
        else -> "This action cannot be undone. All recipes, photos, and journal memories will be permanently wiped."
    }

    fun getCloseLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schließen"
        LanguageMode.FRENCH -> "Fermer"
        LanguageMode.ITALIAN -> "Chiudi"
        LanguageMode.SPANISH -> "Cerrar"
        LanguageMode.DUTCH -> "Sluiten"
        else -> "Close"
    }

    fun getCancelLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Abbrechen"
        LanguageMode.FRENCH -> "Annuler"
        LanguageMode.ITALIAN -> "Annulla"
        LanguageMode.SPANISH -> "Cancelar"
        LanguageMode.DUTCH -> "Annuleren"
        else -> "Cancel"
    }

    fun getSaveLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Speichern"
        LanguageMode.FRENCH -> "Enregistrer"
        LanguageMode.ITALIAN -> "Salva"
        LanguageMode.SPANISH -> "Guardar"
        LanguageMode.DUTCH -> "Opslaan"
        else -> "Save"
    }
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
