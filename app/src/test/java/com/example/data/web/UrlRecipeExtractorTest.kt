package com.example.data.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlRecipeExtractorTest {

    @Test
    fun testIsSocialPlatform() {
        assertTrue(UrlRecipeExtractor.isSocialPlatform("https://www.instagram.com/reel/DaEIO15pYrA/?igsi=MXhpazhrOHByNmNiaA=="))
        assertTrue(UrlRecipeExtractor.isSocialPlatform("https://instagr.am/p/12345"))
        assertTrue(UrlRecipeExtractor.isSocialPlatform("https://www.tiktok.com/@chef/video/123456789"))
        assertTrue(UrlRecipeExtractor.isSocialPlatform("https://youtube.com/shorts/abcdef"))
        assertTrue(UrlRecipeExtractor.isSocialPlatform("https://www.pinterest.com/pin/123456/"))
        assertFalse(UrlRecipeExtractor.isSocialPlatform("https://www.allrecipes.com/recipe/123/garlic-bread/"))
    }

    @Test
    fun testDecodeHtmlEntities() {
        val encoded = "Pull Apart Garlic Bread &#x2728; &quot;Bake at 370&#xb0;F&quot; &amp; &#064;halikit25"
        val decoded = UrlRecipeExtractor.decodeHtmlEntities(encoded)
        assertEquals("Pull Apart Garlic Bread \u2728 \"Bake at 370\u00B0F\" & @halikit25", decoded)
    }

    @Test
    fun testCleanSocialTitle() {
        val rawIgTitle = "Cookwithhali on Instagram: \"Pull Apart Garlic Bread \u2728 #bread #baking\""
        val cleaned = UrlRecipeExtractor.cleanSocialTitle(rawIgTitle)
        assertEquals("Pull Apart Garlic Bread \u2728", cleaned)

        val rawReelTitle = "Pull Apart Garlic Bread (@halikit25) \u2022 Instagram reel"
        val cleanedReel = UrlRecipeExtractor.cleanSocialTitle(rawReelTitle)
        assertEquals("Pull Apart Garlic Bread", cleanedReel)
    }

    @Test
    fun testExtractPageMetadataFromInstagramHtml() {
        val sampleInstagramHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta property="og:site_name" content="Instagram" />
                <meta property="og:title" content="Cookwithhali on Instagram: &quot;Pull Apart Garlic Bread &#x2728; #bread #garlicbread&quot;" />
                <meta property="og:image" content="https://scontent.cdninstagram.com/v/t51.82787-15/sample.jpg?stp=cmp1&amp;oh=12345" />
                <meta property="og:description" content="469K likes, 1,689 comments - halikit25 on June 26, 2026: &quot;Pull Apart Garlic Bread &#x2728;  Ingredients  * 1 tsp yeast * 1 tbsp sugar * 270g milk (about 1 cup + 2 tbsp) * 380g flour (about 3 cups) * 1 tsp salt * 43g softened butter (about 3 tbsp)  Butter Mixture  * 100g butter, softened * 1 tbsp chopped cilantro * 1 garlic clove, minced * Shredded cheddar cheese  Instructions  1. In a bowl, mix the sugar and yeast. Add the milk, then add the flour and mix until combined. 2. Add the salt and softened butter, then coil fold until the butter is fully incorporated. 3. Cover and let the dough rest for 2 hours or until doubled. 4. Mix the softened butter with the cilantro and minced garlic. 5. Roll the dough into a rectangle and spread the garlic butter mixture evenly over it. Sprinkle with shredded cheddar cheese. 6. Cut the dough in half, stack one half on top of the other, then cut into strips. Transfer the pieces into a loaf pan. 7. Cover and let rest for 20 minutes. 8. Bake at 370&#xb0;F for 30 minutes, or until golden brown. 9. Brush with the remaining garlic butter mixture while still warm.  #bread #garlicbread #baking #breadmaking #cheesebread&quot;. " />
                <meta name="keywords" content="cheesy pull apart garlic bread,easy recipe,garlic bread,baking" />
            </head>
            <body>
                <script>var x = 1;</script>
            </body>
            </html>
        """.trimIndent()

        val meta = UrlRecipeExtractor.extractPageMetadata(sampleInstagramHtml)
        assertNotNull(meta.ogTitle)
        assertTrue(meta.ogTitle!!.contains("Pull Apart Garlic Bread"))
        assertNotNull(meta.ogDescription)
        assertTrue(meta.ogDescription!!.contains("1 tsp yeast"))
        assertTrue(meta.ogDescription!!.contains("Bake at 370\u00B0F for 30 minutes"))
        assertEquals("https://scontent.cdninstagram.com/v/t51.82787-15/sample.jpg?stp=cmp1&amp;oh=12345", meta.ogImage)
        assertEquals("cheesy pull apart garlic bread,easy recipe,garlic bread,baking", meta.metaKeywords)

        val compositeText = UrlRecipeExtractor.buildCompositeRecipeText(sampleInstagramHtml, meta)
        assertTrue(compositeText.contains("RECIPE POST CAPTION / DESCRIPTION:"))
        assertTrue(compositeText.contains("1 tsp yeast"))
        assertTrue(compositeText.contains("Butter Mixture"))
        assertTrue(compositeText.contains("Bake at 370\u00B0F for 30 minutes"))
    }
}
