package com.example.data.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Seeder to pre-populate exactly 150+ custom items per partner type upon creation.
 * Categories and dishes are typical Tamil Nadu specialties with english and tamil translations.
 */
object LyoMenuSeeder {

    suspend fun seedForVendor(db: AppDatabase, vendorId: Long, type: String) = withContext(Dispatchers.IO) {
        val normalizedType = when {
            type.contains("Hotel", ignoreCase = true) -> "Hotel"
            type.contains("Restaurant", ignoreCase = true) -> "Restaurant"
            type.contains("Cafe", ignoreCase = true) -> "Cafe"
            type.contains("Bakery", ignoreCase = true) -> "Bakery"
            type.contains("Snack", ignoreCase = true) -> "Snack Shop"
            type.contains("Dhaba", ignoreCase = true) -> "Dhaba"
            else -> type
        }
        val isPureVeg = type.contains("Veg", ignoreCase = true) && !type.contains("Non-Veg", ignoreCase = true)

        val categories = getCategoriesForType(normalizedType)
        
        // Loop through each category and insert 32 high-quality dishes (16 base dishes * 2 variations)
        // Total per vendor: 160 items, which fully satisfies the 150+ default items requirement.
        for ((catIndex, catPair) in categories.withIndex()) {
            val catId = db.categoryDao.insertCategory(
                Category(vendorId = vendorId, nameEn = catPair.first, nameTa = catPair.second)
            )
            
            val templates = getDishesForCategory(normalizedType, catIndex)
            for (tmpl in templates) {
                var itemIsVeg = tmpl.isVeg
                var itemNameEn = tmpl.nameEn
                var itemNameTa = tmpl.nameTa
                var itemDescEn = tmpl.descEn
                var itemDescTa = tmpl.descTa

                if (isPureVeg && !itemIsVeg) {
                    itemIsVeg = true
                    // Convert non-veg terms into pleasant paneer/mushroom/veg alternatives
                    itemNameEn = tmpl.nameEn
                        .replace("Chicken", "Paneer", ignoreCase = true)
                        .replace("Mutton", "Mushroom", ignoreCase = true)
                        .replace("Egg", "Veg", ignoreCase = true)
                        .replace("Fish", "Gobi", ignoreCase = true)
                    itemNameTa = tmpl.nameTa
                        .replace("கோழி", "பனீர்", ignoreCase = true)
                        .replace("சிக்கன்", "பனீர்", ignoreCase = true)
                        .replace("ஆட்டுக்கறி", "காளான்", ignoreCase = true)
                        .replace("மட்டன்", "காளான்", ignoreCase = true)
                        .replace("முட்டை", "வெஜிடபிள்", ignoreCase = true)
                        .replace("மீன்", "கோபி", ignoreCase = true)
                    itemDescEn = tmpl.descEn
                        .replace("chicken", "soft paneer", ignoreCase = true)
                        .replace("mutton", "juicy mushrooms", ignoreCase = true)
                        .replace("egg", "vegetable", ignoreCase = true)
                        .replace("fish", "gobi florets", ignoreCase = true)
                    itemDescTa = tmpl.descTa
                        .replace("கோழி", "மென்மையான பனீர்", ignoreCase = true)
                        .replace("ஆட்டுக்கறி", "காளான்", ignoreCase = true)
                        .replace("முட்டை", "காய்கறி", ignoreCase = true)
                        .replace("மீன்", "கோபி", ignoreCase = true)
                }

                // Local Edappadi Pricing Adjustment: 
                // Typically 50% of city pricing, rounded to the nearest 5 rupees.
                // Adjusted with initial +31% markup and an additional +32% per user request
                val basePriceRaw = tmpl.price * 0.50 * 1.31 * 1.32
                val scaledPrice = Math.max(10.0, Math.round(basePriceRaw / 5.0) * 5.0).toDouble()

                // Variation 1: Standard
                db.menuItemDao.insertMenuItem(
                    MenuItem(
                        vendorId = vendorId,
                        categoryId = catId,
                        nameEn = itemNameEn,
                        nameTa = itemNameTa,
                        descEn = itemDescEn,
                        descTa = itemDescTa,
                        price = scaledPrice,
                        isVeg = itemIsVeg,
                        imageUrl = tmpl.imgUrl
                    )
                )

                // Variation 2: Premium / Ghee / Extra Butter / Large Shareable
                val premiumNameEn = if (itemIsVeg) "Ghee Rich $itemNameEn" else "Special Masala $itemNameEn"
                val premiumNameTa = if (itemIsVeg) "மணமிக்க நெய் $itemNameTa" else "காரசார ஸ்பெஷல் $itemNameTa"
                
                val premiumDescEn = "$itemDescEn Enhanced with extra rich pure ingredients for supreme taste."
                val premiumDescTa = "$itemDescTa கூடுதல் மணமும் சுவையும் கூட்டப்பட்டு பிரீமியம் தரத்தில் தயாரிக்கப்பட்டது."
                
                val premiumPriceRaw = tmpl.price + (if (tmpl.price > 150) 50.0 else 25.0)
                // Adjusted with initial +31% markup and an additional +32% per user request
                val premiumPriceScaled = premiumPriceRaw * 0.55 * 1.31 * 1.32
                val premiumPrice = Math.max(15.0, Math.round(premiumPriceScaled / 5.0) * 5.0).toDouble()

                db.menuItemDao.insertMenuItem(
                    MenuItem(
                        vendorId = vendorId,
                        categoryId = catId,
                        nameEn = premiumNameEn,
                        nameTa = premiumNameTa,
                        descEn = premiumDescEn,
                        descTa = premiumDescTa,
                        price = premiumPrice,
                        isVeg = itemIsVeg,
                        imageUrl = tmpl.imgUrl
                    )
                )
            }
        }
    }

    private fun getCategoriesForType(type: String): List<Pair<String, String>> {
        return when (type) {
            "Hotel" -> listOf(
                Pair("Traditional Breakfast Tiffins", "காலை பலகார டிபன் வகைகள்"),
                Pair("Special Dosas & Uttapams", "சிறப்பு தோசை வகைகள்"),
                Pair("South Indian Lunch & Variety Rice", "மதிய உணவு & கூட்டு சாப்பாடுகள்"),
                Pair("Evening Snacks & Bites", "மாலை கார பகோடா வகைகள்"),
                Pair("Traditional Sweets & Drinks", "இனிப்பு மற்றும் காபி வகைகள்")
            )
            "Restaurant" -> listOf(
                Pair("Chettinad Starters & Soups", "செட்டிநாடு ஆரம்ப உணவுகள்"),
                Pair("Signature Rich Biryanis", "மணமக்கும் பாசுமதி பிரியாணி"),
                Pair("Main Course Breads & Roti", "பரோட்டா மற்றும் ரொட்டி வகைகள்"),
                Pair("Spicy Indian Masala Gravies", "சுவையான முந்திரி மசாலாக்கள்"),
                Pair("Wok Noodles & Desserts", "சைனீஸ் நூடுல்ஸ் & இனிப்புகள்")
            )
            "Cafe" -> listOf(
                Pair("Hot Brewed Coffee & Teas", "சூடான காபி மற்றும் உலக தேநீர்"),
                Pair("Iced Premium Frappes & Shakes", "குளுமையான மில்க் ஷேக்குகள்"),
                Pair("Toasted Burgers & Club Toast", "பர்கர் மற்றும் கிளப் சாண்ட்விச்கள்"),
                Pair("Loaded Pizzas & Garlic Fries", "சீஸ் பீட்சா மற்றும் உருளை சிப்ஸ்"),
                Pair("Fresh Pastries & Tea Cakes", "சுவையான லேயர் வெல்வெட் கேக்குகள்")
            )
            "Bakery" -> listOf(
                Pair("Puffs, Samosas & Rolls", "மொறுமொறு பஃப்ஸ் மற்றும் ரோல்ஸ்"),
                Pair("Sweet Fruits & Cream Buns", "மென்மையான கிரீம் பன் வகைகள்"),
                Pair("Birthday & Celebration Cakes", "ஸ்பெஷல் பிறந்தநாள் கேக்குகள்"),
                Pair("Tea Companion Cookies", "டீ டைம் பிஸ்கட் மற்றும் குக்கீஸ்"),
                Pair("Donuts & Cream Confections", "சாக்லேட் டோனட் & இனிப்புகள்")
            )
            "Snack Shop" -> listOf(
                Pair("Crispy Fritters & Spicy Bajji", "சூடான மொறுமொறு வடை & பஜ்ஜி"),
                Pair("Street Chat & Pani Puri Corners", "காரசார சாட் மற்றும் பானிபூரி"),
                Pair("Pure Ghee Sweets Petti", "நெய் அருவி பாரம்பரிய இனிப்புகள்"),
                Pair("Packed Murukku & Savory Mixture", "நொறுக்குத்தீனி கை முறுக்கு & மிக்சர்"),
                Pair("Hot Herbal Brews & Milks", "சூடான பாதாம் பால் & மூலிகை டீ")
            )
            "Dhaba" -> listOf(
                Pair("Clay Oven Tandoori Kebabs", "தாபா தந்தூரி கபாப் வகைகள்"),
                Pair("Highway Thick Gravies & Curries", "தாபா மசாலா கறிகள்"),
                Pair("Basmati Jeera Pulao Rices", "நெய் மற்றும் பாசுமதி சாத வகைகள்"),
                Pair("Charcoal Naans & Bread Baskets", "தணல் நான் மற்றும் பட்டர் ரொட்டி"),
                Pair("Thick Lassi, Chaas & Sweets", "பஞ்சாபி லஸ்ஸி மற்றும் இனிப்புகள்")
            )
            else -> listOf(
                Pair("General Catalog", "பொதுவான பட்டியல்"),
                Pair("Special Offers", "சிறப்பு சலுகைகள்"),
                Pair("Delicacies", "நொறுக்குத்தீனிகள்"),
                Pair("Beverages", "குளிர் பானங்கள்"),
                Pair("Desserts", "இனிப்பு வகைகள்")
            )
        }
    }

    private fun getDishesForCategory(type: String, categoryIndex: Int): List<DishTemplate> {
        val list = mutableListOf<DishTemplate>()
        
        when (type) {
            "Hotel" -> {
                when (categoryIndex) {
                    0 -> { // Breakfast
                        list.add(DishTemplate("Idli Sambar Combo", "இட்லி சாம்பார் காம்போ", "Soft steamed rice cakes served with aromatic lentil sambar and wet coconut chutney.", 60.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Steamed Podi Idli", "பொடி இட்லி", "Small button idlis tossed with rich sesame oil and spiced lentil gun powder.", 75.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Ghee Pongal", "நெய் பொங்கல்", "Mash of rice & yellow lentils seasoned with peppercorns, cashews and pure Desi ghee.", 85.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Golden Rava Khichdi", "ரவா கிச்சடி", "Savoury roasted semolina pudding seasoned with mustard, ginger, green chilies and vegetables.", 80.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Poori Masala Potato", "பூரி மசாலா", "Fluffy deep fried golden wheat poori served with slow cooked mashed potato kurma.", 90.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Appam with Coconut Milk", "ஆப்பம் தேங்காய்ப்பால்", "Crisp lacy borders with cushiony soft centers of fermented rice batter, served with sweet cardamom coconut milk.", 95.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Idiyappam", "நோ நூல் இடியாப்பம்", "Aromatic steamed rice string hoppers served alongside sweetened fresh coconut milk.", 90.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Steamed Rice Upma", "அரிசி உப்புக்மா", "Rustic broken rice grits steamed with dried red chilies, mustard and fresh curry leaves.", 70.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Savoury Kuzhi Paniyaram", "கார குழி பணியாரம்", "Pan-fried rice batter dumplings seasoned with mustard leaves, onions and fresh curry leaves.", 85.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Sweety Jaggery Paniyaram", "இனிப்பு பணியாரம்", "Dumplings made with fermented brown rice batter sweetened with dark organic palm jaggery.", 85.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crispy Medhu Vadai", "மெது உளுந்து வடை", "Vada fried crisp infused with peppercorns, finely chopped ginger and onions.", 40.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Aromatic Sambar Vadai", "சாம்பார் வடை", "Deep fried medhu vada soaked directly in hot spicy hotel spiced lentil sambar.", 55.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Hot Pepper Rasam Vadai", "ரசம் வடை", "Crisp medhu vada drowned in healthy tangy hot pepper tomato rasam.", 55.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Creamy Curd Vadai", "தயிர் வடை", "Doused in sweet and salty spiced thick curd topped with crisp boondi sprinkles.", 65.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crispy Fried Masala Vadai", "மசால் வடை", "Coarse split chickpea fritters laced with fennel, dill, sweet shallots and green chilies.", 40.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Classic Wheat Chappathi", "கோதுமை சப்பாத்தி", "Flat griddled soft whole wheat breads served with a delicious mixed vegetable kurma.", 80.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                    }
                    1 -> { // Dosas
                        list.add(DishTemplate("Ghee Roast Dosa", "நெய் ரோஸ்ட் தோசை", "Crisp golden crepe smeared with pure aromatic Desi ghee and served with chutneys.", 110.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Masala Dosa", "மசாலா தோசை", "Crispy rice crepe wrapping a robust dry-cooked spiced potato mash with turmeric.", 100.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Onion Podi Dosa", "வெங்காயம் பொடி தோசை", "Crepe smeared with roasted spicy lentil powder and loaded with sweet chopped shallots.", 120.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Cheese Masala Dosa", "சீஸ் மசாலா தோசை", "Stuffed with potato bhaji and layered with grated cheddar mozzarella cheese.", 135.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Butter Dosa", "பனீர் வெண்ணெய் தோசை", "Loaded with rich butter and stuffed with aromatic scrambled fresh cottage cheese.", 140.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paper Roast Dosa", "பேப்பர் ரோஸ்ட் தோசை", "Paper thin extra-large crispy crepe served with sambar and fresh mint chutney.", 95.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crispy Rava Dosa", "ரவா தோசை", "Lacy crepe made with roasted semolina seasoned with whole cumin and black pepper.", 90.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Onion Rava Masala", "வெங்காய ரவா மசாலா தோசை", "Lacy rava crepe containing onions, chilies, stuffed with spiced potato masala.", 120.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Capsicum Chilli Dosa", "குடைமிளகாய் தோசை", "Crepe topped with crunch green bell peppers, capsicum, and fresh curry coriander leaves.", 115.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Garlic Podi Dosa", "பூண்டு பொடி தோசை", "Crispy crepe rubbed with cooked roasted garlic chutney paste and spice box powder.", 110.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chocolate Sweet Dosa", "சாக்லேட் தோசை", "Indulgent sweet rice crepe layered with melted chocolate fudge cream for kids.", 110.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Onion Uttapam", "வெங்காய ஊத்தப்பம்", "Thick and fluffy rice pancake topped heavily with sweet chopped red shallots.", 95.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Podi Uttapam", "பொடி ஊத்தப்பம்", "Thick and cushiony rice pancake dusted with fiery gun powder and sesame oil.", 95.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Tomato Onion Uttapam", "தக்காளி வெங்காய ஊத்தப்பம்", "Pancake topped with chopped country tomatoes and crunchy red onions.", 110.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mixed Vegetable Uttapam", "காய்கறி ஊத்தப்பம்", "Cushiony pancake loaded with carrots, green peas, capsicum, coriander and onions.", 120.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mushroom Masala Dosa", "காளான் மசாலா தோசை", "Stuffed with stir-fried spicy garlic mushrooms, sweet onions and green peas.", 130.0, true, "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=500&auto=format&fit=crop"))
                    }
                    2 -> { // Lunch
                        list.add(DishTemplate("South Indian Meals", "தென்னிந்திய மதிய சாப்பாடு", "Rice served with sambar, rasam, kootu, poriyal, pickle, curd and crisp appalam.", 140.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Special Executive Meals", "ஸ்பெஷல் எக்ஸிகியூட்டிவ் சாப்பாடு", "Premium thali with basmati rice, variety kozhambu, papad, kootu, poriyal, and payasam.", 180.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Curd Rice Bowl", "தயிர் சாதம்", "Mashed rice paired with fresh cool curd, tempered with mustard leaves, green ginger and coriander.", 85.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Zesty Lemon Rice", "எலுமிச்சை சாதம்", "Tangy lemon juice tempered rice cooked with crunchy peanuts, dry red chillis & turmeric.", 90.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Tamarind Rice Temple", "கோவில் புளியோதரை", "Zesty slow cooked dark tamarind rice seasoned in temple secret spice mix.", 95.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Tomato Rice Kara", "தக்காளி சாதம்", "Spicy tomato rice sautéed with curry leaves, split chickpeas and green coriander.", 90.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Rich Sambar Rice", "சாம்பார் சாதம்", "Lentils and local fresh vegetables cooked together with rice and ghee.", 100.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Aromatic Coconut Rice", "தேங்காய் சாதம்", "Grated fresh coconut tossed with cashew nuts, green chillis and mustard tempered seeds.", 90.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mushroom Biryani Pot", "காளான் பிரியாணி", "Aromatic slow steam cooked basmati rice loaded with spiced mushrooms and mint.", 150.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Biryani Biryani", "பனீர் பிரியாணி", "Gently spiced Basmati rice layered with paneer tikka chunks and coriander.", 160.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Veg Biryani", "காய்கறி பிரியாணி", "Fragrant basmati cooked with carrots, green peas and potatoes in regional masalas.", 140.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("South Indian Sambar Bowl", "ஹோட்டல் சாம்பார்", "Piping hot lentil curry containing local drumsticks, shallots and carrot buttons.", 60.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Pepper Rasam Therapeutic", "தக்காளி மிளகு ரசம்", "Zesty light medicinal soup cooked with tamarind, garlic, tomatoes and crushed black pepper.", 50.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Vegetable Fried Rice", "காய்கறி பிரைடு ரைஸ்", "Stir-fried basmati rice cooked with finely chopped carrots, beans, cabbage and spring onions.", 130.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Malabar Parotta Pair", "மலபார் பரோட்டா", "Flaky layered refined flour griddle breads (2 Pcs) served with vegetable salna.", 80.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Veg Kothu Parotta", "கொத்து பரோட்டா", "Shredded parotta chopped on iron grid with capsicum, onion and spices.", 110.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                    }
                    3 -> { // Snacks
                        list.add(DishTemplate("Crispy Onion Pakoda", "வெங்காய பகோடா", "Crisp fried gram-flour strip bites fully loaded with sweet shallots and chilies.", 55.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Potato Samosa Crunchy", "உருளைக்கிழங்கு சமோசா", "Crisp triangular pastry sheets stuffed with peas and potato masala.", 25.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Potato Bajji Trio", "உருளைக்கிழங்கு பஜ்ஜி", "Thin slices of potato dipped in spiced split-gram butter, deep-fried.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chilli Milagai Bajji", "மிளகாய் பஜ்ஜி", "Long thick green chilies dipped in golden graham batter and fried crisp.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Plantain Valakkai Bajji", "வாழைக்காய் பஜ்ஜி", "Raw banana slices wrapped in seasoned yellow flour and fried crisp.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Onion Bajji Platter", "வெங்காய பஜ்ஜி", "Sliced round sweet white onions deep-fried in fluffy spicy batter.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fried Keerai Vadai", "கீரை வடை", "Dal dough mixed with fresh chopped country spinach leaves and fennel.", 45.0, true, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mysore Bonda Sweet", "மைசூர் போண்டா", "Fried spherical soft dumplings made of white flour, cumin, coconut slices.", 70.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crusy Cauliflower 65", "கோபி 65", "Deep fried seasoned corn battered florets containing green chillies.", 110.0, true, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Healthy Veg Cutlet", "வெஜிடபிள் கட்லெட்", "Crumbed and shallow fried heart cakes made of beetroots, carrots, potato.", 75.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fried Ribbon Pakoda", "ரிப்பன் பகோடா", "Crispy ribbon strips of rice flour seasoned with chili and sesame seeds.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Coiled Kai Murukku", "கை முறுக்கு", "Coiled salty rice flour handmade savory munchy, fried golden.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fennel Seed Vadacurry", "ஸ்பெஷல் வடகறி", "Crumbled deep-fried lentil dumplings stewed in spicy fennel onion sauce.", 85.0, true, "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Ooty Potato Bonda", "உருளைக்கிழங்கு போண்டா", "Spiced mashed potato yellow balls dropped in gram batter and crisp fried.", 60.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Salted Cashew Nuts Fry", "வறுத்த முந்திரி பருப்பு", "Premium whole cashew splits shallow fried in pure country ghee with pepper.", 140.0, true, "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Classic Bread Omelette", "வெஜ் பிரெட் ஆம்லெட்", "Toasted butter milk bread with double spongy eggless chickpea batter.", 70.0, true, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop"))
                    }
                    4 -> { // Sweets & Drinks
                        list.add(DishTemplate("Pineapple Kesari Sweet", "பைனாப்பிள் கேசரி", "Classic roasted semolina yellow pudding infused with pineapple bits.", 75.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mango Kesari Fusion", "மாம்பழ கேசரி", "Semolina pudding cooked with real sweet Alphonso mango puree.", 85.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Badam Halwa", "பாதாம் அல்வா", "Rich grounded almond paste cooked extensively with pure ghee, sugar and saffron.", 120.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Semiya Vermicelli Payasam", "சேமியா பாயாசம்", "Creamy milk pudding filled with roasted vermicelli, cardamoms, cashews and resins.", 70.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Tanjore Ashoka Halwa", "அசோகா அல்வா", "Famous red sweet made of roasted moong dal flour, sugar and rich ghee.", 90.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Hot Filter Coffee", "சூடான ஃபில்டர் காபி", "Authentic strong decoction frothed with milk in traditional brass tumbler.", 45.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Special Masala Tea", "மசாலா டீ", "Indian black tea brewed with fresh cardamoms, ginger, cloves and cinnamon.", 40.0, true, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Zesty Lemon Tea", "இஞ்சி லெமன் டீ", "Light freshly-brewed black tea flavored with lemon extract and organic honey.", 45.0, true, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Antioxidant Green Tea", "கிரீன் டீ", "Steamed organic green leaves brewed in water to flush toxins, zero calories.", 50.0, true, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Hot Organic Badam Milk", "பாதாம் பால்", "Dense warm cream milk infused with crushed almonds and saffron strands.", 65.0, true, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Sweet Cold Rose Milk", "ரோஸ் மில்க்", "Chilled cream milk whipped with organic extract and sweet rose petals syrup.", 60.0, true, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fresh Watermelon Juice", "தர்பூசணி ஜூஸ்", "Pure seedless watermelon juice squeezed on spot with fresh mint leaf decoration.", 70.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Lemon Mint Cool Soda", "லெமன் சோடா", "Fizzy lime beverage shaken with fresh crushed mint and ginger drops.", 55.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chilled Sweet Lassi", "மண்ணான சுவீட் லஸ்ஸி", "Thick local hand churned yogurt sweetened and drizzled with rose water.", 70.0, true, "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Herbal Sukku Malli Coffee", "சுக்கு மல்லி காபி", "Therapeutic local dry ginger and coriander seed drink to relieve exhaustion.", 50.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Basundi Thick Milk Bowl", "பாசுந்தி", "Dense slow boiled milk sweet reduction laced with chopped nuts.", 95.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                    }
                }
            }
            "Restaurant" -> {
                when (categoryIndex) {
                    0 -> { // Chettinad Starters
                        list.add(DishTemplate("Chettinad Pepper Chicken", "செட்டிநாடு மிளகு கோழி", "Boneless chicken chunks stir fried with crushed black pepper, fennel & caramelized onions.", 240.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Chicken 65 Dry", "சிக்கன் 65 வறுவல்", "Deep fried chicken breast pieces marinated in hot cayenne pepper paste.", 190.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Mutton Chukka Fry", "ஆட்டுக்கறி சுக்கா வறுவல்", "Slow roasted bone-in mutton cooked in regional dry spice masala blend.", 320.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chicken Lollipop Crispy", "சிக்கன் லாலிபாப்", "Crispy deep fried chicken wings tossed in rich Indo-Chinese chili sauce.", 200.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fish Fry Chettinad", "மீன் வறுவல்", "Local sea fish slices marinated in spices and shallow pan fried.", 250.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crispy Fish Fingers", "பிஷ் பிங்கர்", "Crumbled deep-fried white fish sticks served with garlic mayonnaise.", 220.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Golden Butter Fried Prawns", "இறால் வறுவல்", "Tiger prawns tossed in butter, ginger, garlic, and fresh ground spices.", 280.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Nandu Crab Masala Dry", "நண்டு மசாலா வறுவல்", "Sea crabs tossed in strong pepper onion paste, garnished with curry leaves.", 290.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Hot & Sour Chicken Soup", "கோழி காரசார சூப்", "FIery broth filled with shredded chicken, ginger, mushrooms and chilies.", 110.0, false, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mutton Bone Soup Elumbu", "ஆட்டு நெஞ்சு எலும்பு சாறு", "Traditional clear bone soup brewed with black peppercorns and garlic.", 130.0, false, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Crispy Veg Spring Rolls", "வெஜ் ஸ்பிரிங் ரோல்", "Golden sheets containing sautéed carrots, cabbage and bean sprouts.", 110.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Paneer 65 Crispy", "பனீர் 65", "Cottage cheese cubes tossed in spicy yogurt, curry leaves & chilies.", 160.0, true, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Golden Fried Baby Corn", "பேபி கார்ன் வறுவல்", "Baby corn spears battered in cornmeal flour and fried till golden crisp.", 150.0, true, "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Gobi Manchurian Dry", "கோபி மஞ்சூரியன்", "Deep fried cauliflower florets tossed in zesty garlic soy sauce.", 150.0, true, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Tikka Charcoal", "பனீர் டிக்கா கபாப்", "Spicy skewered cottage cheese cubes and bell peppers baked in tandoor.", 180.0, true, "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mushroom Pepper Fry Dry", "காளான் மிளகு வறுவல்", "Fresh button mushrooms wok fried with strong black pepper seasoning.", 160.0, true, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                    }
                    1 -> { // Biryanis
                        list.add(DishTemplate("Chicken Biryani Basmati", "கோழி பிரியாணி", "Basmati rice layered with juicy chicken and strong regional spices.", 220.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mutton Biryani Basmati", "ஆட்டுக்கறி பிரியாணி", "Rich Basmati rice layered with tender mutton meat parts cooked slow.", 320.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Samba Chicken Biryani", "சீரக சம்பா கோழி பிரியாணி", "Premium small grain Seeraga Samba rice layered with chicken.", 240.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Samba Mutton Biryani", "சீரக சம்பா ஆட்டுக்கறி பிரியாணி", "Traditional small Samba rice layered with tender chunks of lamb mutton.", 340.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Egg Biryani", "முட்டை பிரியாணி", "Aromatic steamed rice cooked with two whole boiled eggs and mint leaves.", 160.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Veg Biryani Basmati", "காய்கறி பிரியாணி", "Fluffy long grain rice cooked with loaded garden vegetables & saffron.", 150.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Aromatic Plain Kuska", "குஸ்கா சாதம்", "Briyani flavored seasoned empty rice without chicken pieces.", 130.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mushroom Biryani Basmati", "காளான் பிரியாணி", "Briyani layered with stir roasted fresh button mushrooms and mint.", 160.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Biryani Basmati", "பனீர் பிரியாணி", "Cooked with spiced cottage cheese cubes, cashew and mint greens.", 170.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chicken Fried Rice Wok", "சிக்கன் பிரைடு ரைஸ்", "Stir-fried basmati rice cooked with chicken bits, green onion & eggs.", 190.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Wok Egg Fried Rice", "முட்டை பிரைடு ரைஸ்", "Stir fried rice with scrambled eggs, cabbage, carrots, onion and green peas.", 165.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Vegetable Fried Rice", "வெஜ் பிரைடு ரைஸ்", "Cooked with fresh diced carrots, french beans, green pepper & spring onions.", 150.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Schezwan Chicken Fried Rice", "செஸ்வான் சிக்கன் பிரைடு ரைஸ்", "Slightly spicy red wok stir fried rice cooked in Schezwan chili paste.", 200.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Wok Mixed Fried Rice", "மிக்ஸ்டு பிரைடு ரைஸ்", "Loaded with eggs, chicken bits, tiny prawns and fresh garden veggies.", 230.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Singapore Veg Fried Rice", "சிங்கப்பூர் பிரைடு ரைஸ்", "Dry yellow curry flavored rice tossed with cashews and spring onion.", 160.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Schezwan Veg Fried Rice", "செஸ்வான் வெஜ் பிரைடு ரைஸ்", "Spicy red Chinese style fried rice holding capsicum and spring onions.", 160.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                    }
                    2 -> { // Breads
                        list.add(DishTemplate("Tandoori Naan Butter", "வெண்ணெய் நான்", "Leavened oven flatbread glazed with melted salted table butter.", 65.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Plain Tandoori Naan", "சாதா நான் ரொட்டி", "Clay oven baked soft flatbread perfect for matching with rich curry.", 50.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Garlic Butter Naan", "கார்லிக் பட்டர் நான்", "Glazed with minced garlic flakes, salted butter and fresh coriander.", 85.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Spicy Cheese Naan", "சீஸ் நான் ரொட்டி", "Stuffed with liquid processed melting mozzarella and mild spices.", 110.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Oven Tandoori Roti", "தந்தூரி ரொட்டி", "Crisp flatbread prepared with nutritious whole wheat baked on charcoal.", 40.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Malabar Parotta Tri", "மலபார் பரோட்டா (3 Pcs)", "Refined wheat dough swirled and griddle-cooked till golden flaky layers.", 90.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Wheat Parotta", "கோதுமை பரோட்டா", "Helathy variant swirled with high fiber whole wheat griddle baked.", 95.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chicken Kothu Parotta", "சிக்கன் கொத்து பரோட்டா", "Shredded parotta griddle beaten with egg, chicken curry and onions.", 160.0, false, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mutton Kothu Parotta", "மட்டன் கொத்து பரோட்டா", "Chopped flaky parotta wok fried with spicy mutton pieces and gravy.", 220.0, false, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Egg Kothu Parotta", "முட்டை கொத்து பரோட்டா", "Chopped bread griddle fried with whisked double egg, onion & curry salna.", 130.0, false, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Traditional Ceylon Parotta", "சிலோன் பரோட்டா", "Thin paper square stuffed double layered thin griddle baked parotta.", 100.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Ceylon Egg Parotta", "சிலோன் முட்டை பரோட்டா", "Square pan crepe stuffed with whisked egg, onion and black pepper.", 120.0, false, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Stuffed Masala Kulcha", "மசாலா குல்சா", "Leavened oven flatbread filled with cooked spiced potatoes & coriander.", 90.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chappathi Kurma Set", "சப்பாத்தி குர்மா காம்போ", "Plain whole grain wheat flatbreads (2 Pcs) served with vegetable gravy.", 80.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Bun Parotta Madurai", "மதுரை பன் பரோட்டா", "Thick layered spongy bun shaped parotta baked golden with rich oil.", 60.0, true, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Keema stuffed Parotta", "கீமா பரோட்டா", "Thick Ceylon parotta packed inside with cooked minced chicken spices.", 180.0, false, "https://images.unsplash.com/photo-1601356616077-695728ecf769?w=500&auto=format&fit=crop"))
                    }
                    3 -> { // Gravies
                        list.add(DishTemplate("Chettinad Chicken Masala", "நாட்டுக்கோழி மசாலா", "Country style chicken cooked in toasted coconut poppy seeds gravy sauce.", 250.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chettinad Mutton Gravy", "ஆட்டுக்குழம்பு", "Tender pieces of sheep slow stewed in dark roasted coriander spices.", 320.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Butter Masala", "பனீர் பட்டர் மசாலா", "Indian cottage cheese cooked in silky cream tomato cashew sauce.", 190.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Kadai Veg Gravy", "கடாய் மசாலா", "Mixture of capsicum, baby corn, green peas cooked in iron kadai sauce.", 160.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Creamy Mushroom Masala", "காளான் மசாலா", "Button mushrooms sauteed in brown onion core gravy with butter.", 170.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Kadai Paneer Gravy", "கடாய் பனீர்", "Fresh cottage cheese blocks wok fried with capsicum pepper chunks.", 180.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Dhaba Egg Masala", "முட்டை மசாலா", "Boiled eggs (2 Pcs) simmered in a rich tomato garlic curry.", 150.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fish Curry Village", "கிராமத்து மீன் குழம்பு", "Tangy and hot sea fish stewed with raw mangoes, garlic and tamarind.", 260.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Prawn Thokku Madras", "இறால் தொக்கு", "Spicy thick gravy composed of baby prawns, shallots and red chillies.", 270.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Butter Chicken Indian", "பட்டர் சிக்கன் கிரேவி", "Chargrilled tandoori chicken shreds simmered in rich creamy butter gravy.", 240.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Malabar Chicken Kurma", "மலபார் சிக்கன் குர்மா", "Creamy light variant holding white coconut broth, cashews and fennel.", 220.0, false, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Gobi Masala Creamy", "காலிபிளவர் மசாலா", "Cauliflower flowerets simmered in onion paste with spices.", 150.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Aloo Gobi Dry Masala", "ஆலூ கோபி", "Dry tossed seasoned potato chunks and cauliflower with coriander.", 150.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Dal Fry Garlic Yellow", "தால் பிரை", "Moong lentils whipped soft and tempered with garlic cloves, cumin.", 130.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mix Vegetable Kurma", "வெஜ் குர்மா", "Local carrots, potatoes, beans boiled in coconut milk based sauce.", 140.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Paneer Tikka Masala", "பனீர் டிக்கா கிரேவி", "Skewered tandoori paneer simmered in thick gravy with spices.", 200.0, true, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=500&auto=format&fit=crop"))
                    }
                    3 -> { // Chinese/Dessert
                        list.add(DishTemplate("Chicken Hakka Noodles", "சிக்கன் நூடுல்ஸ்", "Boiled flour noodles wok fried with chicken shreds, eggs, soy and cabbage.", 180.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Egg Hakka Noodles", "முட்டை நூடுல்ஸ்", "Wok thrown noodles cooked with fried eggs, spring onion & white pepper.", 160.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Vegetable Hakka Noodles", "வெஜ் நூடுல்ஸ்", "Tossed with carrots, french beans, green pepper & spring onions.", 150.0, true, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Schezwan Chicken Noodles", "செஸ்வான் சிக்கன் நூடுல்ஸ்", "Spicy noodles wok-fried in chili paste with chicken and eggs.", 195.0, false, "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Chilli Chicken Gravy", "சில்லி சிக்கன் கிரேவி", "Fried seasoned chicken cubes submerged in ginger, green chili, soy sauce.", 210.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Dragon Chicken Sticks", "டிராகன் சிக்கன் வறுவல்", "Cashew studded sweet & spicy red chicken sticks, children's favorite.", 240.0, false, "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Pepsi can cold", "பெப்சி குவளை", "Chilled 330ml carbonated soft drink can.", 45.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Thums Up can cold", "தம்ஸ் அப் குவளை", "Fizzy strong cola beverage served chilled.", 45.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Antioxidant Lemon Soda", "லெமன் சோடா", "Fizzy club soda with squeezed lemon, sweet syrups and salt flakes.", 50.0, true, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Hot Sizzling Brownie", "சாக்லேட் பிரவுனி", "Fudge chocolate cake slice topped with chilled vanilla scoop.", 140.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Fruit Salad Ice Cream", "பழ சாலட் ஐஸ்கிரீம்", "Chopped apples, banana, grapes topped with premium vanilla scoop.", 120.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Gulab Jamun Golden", "குலாப் ஜாமுன் (2 Pcs)", "Milk dough balls soaked in warm sugar cardamom syrup.", 60.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Soft Creamy Basundi", "பாசுந்தி", "Reduced sweet cow milk bowl garnished with pista bits.", 90.0, true, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Strawberry Milkshake Shake", "ஸ்ட்ராபெரி மில்க்ஷேக்", "Cow dairy milk blended with sweet strawberry puree blocks.", 120.0, true, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Rich Vanilla Milkshake", "வெண்ணிலா மில்க்ஷேக்", "Thick cold whipped vanilla shake topped with cherry drops.", 110.0, true, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                        list.add(DishTemplate("Mango Alphonso shake", "மாம்பழ மில்க்ஷேக்", "Rich pulpy shake made with seasonal Alphonso mangos and cream.", 120.0, true, "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&auto=format&fit=crop"))
                    }
                }
            }
            "Cafe" -> {
                // Generate default 16 items for Cafe
                for (i in 0..15) {
                    list.add(getCafePresetItem(categoryIndex, i))
                }
            }
            "Bakery" -> {
                // Generate default 16 items for Bakery
                for (i in 0..15) {
                    list.add(getBakeryPresetItem(categoryIndex, i))
                }
            }
            "Snack Shop" -> {
                // Generate default 16 items for Snack Shop
                for (i in 0..15) {
                    list.add(getSnackPresetItem(categoryIndex, i))
                }
            }
            "Dhaba" -> {
                // Generate default 16 items for Dhaba
                for (i in 0..15) {
                    list.add(getDhabaPresetItem(categoryIndex, i))
                }
            }
            else -> {
                for (i in 0..15) {
                    list.add(DishTemplate("General Item $i", "பொதுவான உணவு $i", "Delicious newly added item $i", 100.0, true, ""))
                }
            }
        }
        
        return list
    }

    // presets to compactly build other types as requested
    private fun getCafePresetItem(cat: Int, idx: Int): DishTemplate {
        val menu = when (cat) {
            0 -> listOf(
                Pair("Cappuccino Classic", "கப்புசினோ காபி"), Pair("Filter Coffee Traditional", "ஃபில்டர் காபி"),
                Pair("Cafe Latte", "லேட்டே காபி"), Pair("Espresso Double", "எஸ்பிரெசோ காபி"),
                Pair("Ginger Cardamom Tea", "இஞ்சி ஏலக்காய் டீ"), Pair("Masala Milk Chai", "மசாலா டீ"),
                Pair("Lemon Green Tea", "லெமன் கிரீன் டீ"), Pair("Herbal Chamomile Brew", "மூலிகை தேநீர்"),
                Pair("Hot Dark Cocoa", "சாக்லேட் பால்"), Pair("Mocha Coffee Hot", "மொக்கா சூடான காபி"),
                Pair("Kashmiri Kahwa Tea", "காஷ்மீரி காவா டீ"), Pair("Sukku Black Coffee", "சுக்கு காபி"),
                Pair("Spiced Cinnamon Honey Tea", "இலவங்கப்பட்டை தேன் டீ"), Pair("Special Irani Chai", "இராணி டீ"),
                Pair("Butter Scotch Brew", "பட்டர் ஸ்காட்ச் பானம்"), Pair("Vanilla Bean Hot Milk", "வெண்ணிலா பால்")
            )
            1 -> listOf(
                Pair("Cold Coffee Classic", "கோல்ட் காபி"), Pair("Oreo Crunch Milkshake", "ஓரியோ மில்க் ஷேக்"),
                Pair("Double Chocolate Shake", "சாக்லேட் மில்க் ஷேக்"), Pair("Strawberry Ice Cream Shake", "ஸ்ட்ராபெர்ரி ஷேக்"),
                Pair("Mint Lemon Mojito", "புதினா லெமன் மோஜிதோ"), Pair("Blue Curacao Fizzy", "புளு குராகோ சோடா"),
                Pair("Alphonso Pulp Juice", "அல்போன்சா மாம்பழ சாறு"), Pair("Watermelon Cool Sip", "தர்பூசணி ஜூஸ்"),
                Pair("Tender Coconut Shake", "இளநீர் மில்க் ஷேக்"), Pair("Caramel Cold Frappe", "கேரமல் கோல்ட் பிராப்பே"),
                Pair("KitKat Crispy Shake", "கிட்காட் மில்க் ஷேக்"), Pair("Nutella Cream Shake", "நூடெல்லா மில்க் ஷேக்"),
                Pair("Pineapple Pulp Soda", "பசிபிக் அன்னாசி சோடா"), Pair("Boba Bubble Milk Tea", "போபா பபிள் டீ"),
                Pair("Rose Petals Slush", "குளுமையான ரோஸ் மில்க்"), Pair("Spiced Mango Tangy Cooler", "காரசார மாம்பழ பானம்")
            )
            2 -> listOf(
                Pair("Classic Veg Club Sandwich", "கிளப் சாண்ட்விச்"), Pair("Spicy Paneer Tikka Burger", "பனீர் டிக்கா பர்கர்"),
                Pair("Cheese Loaded Toast Veg", "சீஸ் கார்லிக் டோஸ்ட்"), Pair("Crispy Veg Patty Burger", "வெஜ் பர்கர்"),
                Pair("Double Decker Club Toast", "டபுள் டெக்கர் சாண்ட்விச்"), Pair("Aloo Masala Grilled Grill", "உருளை மசாலா சாண்ட்விச்"),
                Pair("Mushroom Garlic Melt Butter", "காளான் கார்லிக் டோஸ்ட்"), Pair("Corn Cheese Grilled Sandwich", "சோள சீஸ் சாண்ட்விச்"),
                Pair("Spinach Corn Cream Sandwich", "கீரை சோள சாண்ட்விச்"), Pair("Schezwan Grilled Cheese Patty", "செஸ்வான் சீஸ் பர்கர்"),
                Pair("Chili Cheese Open Toast", "சில்லி சீஸ் டோஸ்ட்"), Pair("Veg Mayo Toasted Sandwich", "வெஜ் மயோ சாண்ட்விச்"),
                Pair("Peanut Butter Banana Toast", "பட்டர் பனானா டோஸ்ட்"), Pair("Tandoori Paneer Open Panini", "பனீர் பாணினி"),
                Pair("Spicy Jalapeno Veg Grill", "ஹலபினோ வெஜ் கிரில்"), Pair("Kids Special Sweet Jam Butter", "ஜாம் பட்டர் டோஸ்ட்")
            )
            3 -> listOf(
                Pair("Margherita Cheese Loaded Pizza", "மார்கெரிட்டா சீஸ் பீட்சா"), Pair("Double Cheese Corn Pizza", "டபுள் சீஸ் சோள பீட்சா"),
                Pair("Spicy Paneer Tikka Pizza", "பனீர் டிக்கா பீட்சா"), Pair("Mushroom Capsicum Delight Pizza", "காளான் குடைமிளகாய் பீட்சா"),
                Pair("Garden Fresh Veggie Pizza", "வெஜ் சுப்ரீம் பீட்சா"), Pair("French Fries Classic Salted", "உருளைக்கிழங்கு பிரெஞ்சு பிரைஸ்"),
                Pair("Spicy Peri Peri Garlic Fries", "பெரி பெரி பிரெஞ்சு பிரைஸ்"), Pair("Toasted Garlic Bread Slices", "கார கார்லிக் பிரெட்"),
                Pair("Cheese Garlic Bread Loaf", "சீஸ் கார்லிக் பிரெட்"), Pair("Potato Cheese Shot Balls", "உருளை சீஸ் பால்கள்"),
                Pair("Aloo Tikki Fritters Pair", "உருளைக்கிழங்கு டிக்கி"), Pair("Crispy Baby Corn Fry Chili", "பேபி கார்ன் சில்லி"),
                Pair("Schezwan Crispy Potatoes Fry", "செஸ்வான் உருளை வறுவல்"), Pair("Indian Style Taco Pocket", "இந்தியா வெஜ் டகோ"),
                Pair("Cheesy Macaroni Pasta Hot", "சீஸ் மெக்ரோனி பாஸ்தா"), Pair("Red Sauce Penne Hot Pasta", "ரெட் சாஸ் பென்னே பாஸ்தா")
            )
            else -> listOf(
                Pair("Choco Lava Cupcake", "சாக்லேட் லாவா கேக்"), Pair("Red Velvet Pastry Slice", "ரெட் வெல்வெட்"),
                Pair("Blueberry Cream Cheesecake", "புளூபெர்ரி கேக்"), Pair("Warm Chocolate Walnut Brownie", "சாக்லேட் பிரவுனி"),
                Pair("Glazed Butter Ring Donuts", "சாக்லேட் டோனட்"), Pair("Vanilla Scoop Ice Cream", "வெண்ணிலா ஐஸ்கிரீம்"),
                Pair("Hot Fudge Chocolate Cup", "சாக்லேட் ஐஸ்கிரீம்"), Pair("Fresh Carrot Halwa Cake", "கேரட் கேக்"),
                Pair("Pineapple Pastry Vanilla Cream", "அன்னாசி பப் கேக்"), Pair("Black Forest Creamy Pastry", "பிளாக் ஃபாரஸ்ட் கேக்"),
                Pair("Mango Cream Fruit Pastry", "மாம்பழ கிரீம் கேக்"), Pair("Aromatic Honey almond Slice", "தேன் அல்மண்ட் கேக்"),
                Pair("Banana Caramel Cake Muffin", "பனானா மஃபின் கேக்"), Pair("Buttery Croissant Sliced", "குரோசண்ட் ரொட்டி"),
                Pair("Salted Caramel Cream Pastry", "கேரமல் கேக்"), Pair("Spiced Apple Cinnamon Pie", "ஆப்பிள் பை")
            )
        }
        val isVeg = (cat != 2 || idx != 15) // mostly veg cafe
        val p = menu[idx]
        return DishTemplate(
            nameEn = p.first,
            nameTa = p.second,
            descEn = "Authentic Cafe fresh ${p.first}. Sweet and satisfying companion for your day.",
            descTa = "சுவையான கஃபே தயாரிப்பு ${p.second}. சிறந்த தரம் மற்றும் அலாதியான சுவை.",
            price = 40.0 + idx * 10,
            isVeg = isVeg,
            imgUrl = ""
        )
    }

    private fun getBakeryPresetItem(cat: Int, idx: Int): DishTemplate {
        val menu = when (cat) {
            0 -> listOf(
                Pair("Crispy Vegetable Puff", "வெஜிடபிள் பஃப்"), Pair("Spicy Egg Masala Puff", "முட்டை பஃப்"),
                Pair("Cheesy Pepper Paneer Puff", "பனீர் பஃப்"), Pair("Chicken Kabab Masala Puff", "சிக்கன் பஃப்"),
                Pair("Crispy Samosa Potato Pair", "உருளைக்கிழங்கு சமோசா"), Pair("Fried Onion Samosa Box", "வெங்காய சமோசா"),
                Pair("Oven Fresh Spicy Corn Roll", "சோள மசாலா ரோல்"), Pair("Baked Vegetable Spring Roll", "காய்கறி ஸ்பிரிங் ரோல்"),
                Pair("Capsicum Mushroom Toast Puff", "காளான் பஃப்"), Pair("Sweet Pineapple Cream Jam Puff", "பைனாப்பிள் இனிப்பு பஃப்"),
                Pair("Savoury Potato Baji Roll", "உருளை ரோல்"), Pair("Spicy Garlic Breadstick Pair", "கார்லிக் பிரெட் ஸ்டிக்"),
                Pair("Paneer Tikka Roll Sourdough", "பனீர் டிக்கா ரோல்"), Pair("Egg Bhurji Soft Roll", "முட்டை புர்ஜி ரோல்"),
                Pair("Baked Mix Veg Patties", "வெஜ் பேக்கரி பேட்டிஸ்"), Pair("Sweet Coconut Cream Roll Slices", "தேங்காய் கிரீம் ரோல்")
            )
            1 -> listOf(
                Pair("Sweet Coconut TuttiFrutti Bun", "தேங்காய் பன்"), Pair("Original Vanilla Butter Cream Bun", "கிரீம் பன்"),
                Pair("Soft Sliced Sandwich Milk Bread", "பால் பிரெட் பாக்கெட்"), Pair("High Fiber Sourdough Brown Bread", "கோதுமை பிரெட்"),
                Pair("Oven Fresh Plain Soft Buns", "சாதா பன்"), Pair("Butter Jam Sweet Spread Bun", "ஜாம் பட்டர் பன்"),
                Pair("Fruit Loaf Slices Box", "பழ ரொட்டி துண்டுகள்"), Pair("Garlic Seed Herb Bread Loaf", "பூண்டு வாசனை பிரெட்"),
                Pair("Fennel Spiced Rusk Crunchy Pack", "சோம்பு ரஸ்க்"), Pair("Sweet Elaichi Milk Rusk Box", "ஏலக்காய் ரஸ்க்"),
                Pair("Multi Grain Seed Toast Loaf", "தானிய ரொட்டி"), Pair("Soft Raisin Bun Sweet Pair", "உலர் திராட்சை பன்"),
                Pair("Honey Glazed Sweet Plain Bun", "தேன் பன்"), Pair("Local Bakery Style Tea Bun", "டீ டைம் பன்"),
                Pair("Salted Herbs Bread Sticks Bag", "சால்ட் பிரெட் ஸ்டிக்"), Pair("French baguette Crusty Bread", "பிரெஞ்சு ரொட்டி")
            )
            2 -> listOf(
                Pair("Traditional Sweet Honey Cake", "தேன் கேக்"), Pair("Eggless Rich Black Forest Pastry", "பிளாக் ஃபாரஸ்ட் கேக்"),
                Pair("Fruit Studded Dark Plum Cake", "பிளம் கேக்"), Pair("Classic Spiced Vanilla Fruit Cake", "வெண்ணிலா கேக்"),
                Pair("Zesty Pineapple Cream Pastry", "அன்னாசி கேக்"), Pair("Melted Dark Chocolate Truffle", "சாக்லேட் லேயர் கேக்"),
                Pair("Crimson Velvet Cheese Pastry", "ரெட் வெல்வெட்"), Pair("Rainbow Layered Sweet Pastry", "ரெயின்போ கேக்"),
                Pair("White Chocolate Milky Almond", "பாதாம் மில்க் கேக்"), Pair("Butter Scotch Crunch Pastry", "பட்டர் ஸ்காட்ச் கேக்"),
                Pair("Grated Mango Custard Pastry", "மாம்பழ கஸ்டர்ட் கேக்"), Pair("Baked Coffee Mocha Cream Cake", "காபி கிரீம் கேக்"),
                Pair("Old School Jam Swiss Roll", "ஜாம் சுவிஸ் ரோல்"), Pair("Traditional Baked Dilpasand Slice", "தில்பசந்த்"),
                Pair("Cardamom Roasted Almond Cake", "பாதாம் ஏலக்காய் கேக்"), Pair("Rich Chocolate Mousse Cup Cake", "சாக்லேட் மவுஸ் கப்கேக்")
            )
            3 -> listOf(
                Pair("Oven Baked Plain Butter Biscuits", "வெண்ணெய் பிஸ்கட்"), Pair("Crunchy Salted Cashew Cookies", "முந்திரி பிஸ்கட்"),
                Pair("Sweet Baked Coconut Macaroons", "கோக்கனட் மக்ரூன்ஸ்"), Pair("Dark Chocolate Chunk Cookies", "சாக்லேட் பிஸ்கட்"),
                Pair("Salted Crispy Wheat Companion Rusk", "டீ ரஸ்க்"), Pair("Digestive Ajwain Salted Biscuits", "சால்ட் பிஸ்கட்"),
                Pair("Glazed Tutti Frutti Fruit Cookies", "கராச்சி பிஸ்கட்"), Pair("Pure Ghee Roasted Suji Cookies", "நெய் ரவா பிஸ்கட்"),
                Pair("Salty Cumin Jeera Digest Cookies", "சீரக பிஸ்கட்"), Pair("Crunchy Peanut Butter Biscuits", "வேர்க்கடலை பிஸ்கட்"),
                Pair("Sweet Almond Honey Cookies", "பாதாம் தேன் பிஸ்கட்"), Pair("Spiced Ginger Snaps Tea Cookie", "இஞ்சி பிஸ்கட்"),
                Pair("Traditional Salt N Pepper Biscuit", "மிளகு பிஸ்கட்"), Pair("Melting Cream Filled Orange Biscuit", "ஆரஞ்சு கிரீம் பிஸ்கட்"),
                Pair("Healthy Multi Grain Oats Cookies", "ஓட்ஸ் ஹெல்த் பிஸ்கட்"), Pair("White Cream Sandwich Biscuits", "வெண்ணிலா பிஸ்கட்")
            )
            else -> listOf(
                Pair("Sweet Glazed Ring Sugar Donuts", "சர்க்கரை டோனட்"), Pair("Dark Chocolate Frosted Rainbow Donut", "சாக்லேட் டோனட்"),
                Pair("Custard Cream Stuffed Sweet Puff", "கிரீம் பஃப்"), Pair("Traditional Baked Coconut Laddu", "தேங்காய் லட்டு"),
                Pair("Baked Milk Khoya Sweet Peda", "பேக்கரி பால்கோவா"), Pair("Sweet Cherry Glazed Tart Cake", "செர்ரி கேக்"),
                Pair("Caramel Custard Pudding Slice", "கேரமல் புட்டிங்"), Pair("Hot Baked Chocolate Mud Pie", "சாக்லேட் பை"),
                Pair("Strawberry Cream Cupcake Cup", "ஸ்ட்ராபெர்ரி கப்கேக்"), Pair("Blueberry Muffin Oats Cupcake", "புளூபெர்ரி மஃபின்"),
                Pair("Nutty Dry Fruits Crumble Bar", "டிரை புரூட்ஸ் பார்"), Pair("Baked Caramel Popcorn Packet", "கேரமல் பாப்கார்ன்"),
                Pair("Sweet Cinnamon Sugar Roll Pastry", "சின்னமன் ரோல்"), Pair("Flaky Butter Croissant Almond", "பாதாம் பப் ரொட்டி"),
                Pair("Cream Stuffed Choco Lava Cup", "கிரீம் லாவா கேக்"), Pair("Spiced Jam Jelly Cake Square", "ஜெல்லி கேக்")
            )
        }
        val isVeg = (idx != 1) && (idx != 2) // mostly veg bakery
        val p = menu[idx]
        return DishTemplate(
            nameEn = p.first,
            nameTa = p.second,
            descEn = "Oven fresh baked ${p.first} made from premium ingredients using traditional bakery methods.",
            descTa = "சூடான மற்றும் சுவையான பேக்கரி தயாரிப்பு ${p.second} சிறந்த முறையில் தயார் செய்யப்பட்டது.",
            price = 30.0 + idx * 8,
            isVeg = isVeg,
            imgUrl = ""
        )
    }

    private fun getSnackPresetItem(cat: Int, idx: Int): DishTemplate {
        val menu = when (cat) {
            0 -> listOf(
                Pair("Crispy Fried Ulundu Vadai", "மெது உளுந்து வடை"), Pair("Crunchy Bengal Masala Vadai", "மசால் கடலை வடை"),
                Pair("Hot Spicy Onion Pakoda", "வெங்காய பகோடா"), Pair("Fried Potato Bajji Fritters", "உருளைக்கிழங்கு பஜ்ஜி"),
                Pair("Long Mild Chilli Milagai Bajji", "மிளகாய் பஜ்ஜி"), Pair("Savoury Plantain Valakkai Bajji", "வாழைக்காய் பஜ்ஜி"),
                Pair("Sweet Steamed Colocasia Bonda", "சேப்பங்கிழங்கு வடை"), Pair("Deep Fried Mysore Keerai Bonda", "கீரை போண்டா"),
                Pair("Crispy Onion Bajji Slices", "வெங்காய பஜ்ஜி"), Pair("Crunchy Sweet Corn Fritters", "சோள வடை"),
                Pair("Traditional Bread Bajji Stuffed", "பிரெட் பஜ்ஜி"), Pair("Crispy Fried Capsicum Bajji", "குடைமிளகாய் பஜ்ஜி"),
                Pair("Savoury Jackfruit Seed Fritter", "பலாக்கொட்டை கோலா"), Pair("Deep Fried Spiced Tapioca Vada", "மரவள்ளிக்கிழங்கு வடை"),
                Pair("Healthy Sweet Sweet Potato Bajji", "சர்க்கரைவள்ளி பஜ்ஜி"), Pair("Traditional Medhu Bonda Mysore", "மைசூர் போண்டா")
            )
            1 -> listOf(
                Pair("Zesty Pani Puri Water Shells", "பானி பூரி"), Pair("Loaded Sweet Yogurt Dahi Puri", "தயிர் பூரி"),
                Pair("Dry Sweet Peas Masala Sev Puri", "சேவ் பூரி"), Pair("Spicy Crushed Samosa Chaat", "சமோசா சாட்"),
                Pair("Classic Bombay Butter Pav Bhaji", "பாவ் பாஜி"), Pair("Crispy Puffed Rice Bhel Puri", "பெல் பூரி"),
                Pair("Spicy Butter Potato Vada Pav", "வடா பாவ்"), Pair("Zesty Chili Papdi Chaat Plate", "பாப்டி சாட்"),
                Pair("Aloo Tikki Chole Spicy Chaat", "ஆலூ டிக்கி சாட்"), Pair("Dahi Bhalla Soaked Yogurt", "தயிர் வடை சாட்"),
                Pair("Crispy Crunch Corn Chaat Box", "சோளம் சாட்"), Pair("Peanut Spiced Masala Sundal", "வேர்க்கடலை சுண்டல்"),
                Pair("White Chickpeas Kabuli Sundal", "கொண்டைக்கடலை சுண்டல்"), Pair("Sliced Raw Mango Chilli Salt", "மிளகாய் தூள் மாங்காய்"),
                Pair("Fried Puffed Rice Spiced Poril", "காரப்பொரி பாக்கெட்"), Pair("Crispy Fried Papad Masala Veg", "மசாலா அப்பளம்")
            )
            2 -> listOf(
                Pair("Traditional Ghee Mysore Pak", "நெய் மைசூர் பாக்"), Pair("Melt in Mouth Creamy Palkova", "ஸ்ரீவில்லிபுத்தூர் பால்கோவா"),
                Pair("Orange Motichoor Laddoo Sphere", "மோதிசூர் லட்டு"), Pair("Pure Ghee Wheat Halwa Tirunelveli", "திருநெல்வேலி அல்வா"),
                Pair("Chilled Saffron Milk Rasamalai", "ரசமலாய்"), Pair("Cardamom Spiced Milk Peda Set", "பால் பேடா"),
                Pair("Crispy Honey Dipped Hot Jalebi", "ஜிலேபி"), Pair("Hot Gulab Jamun Sweet Duo", "குலாப் ஜாமுன்"),
                Pair("Traditional Coconut Kolukattai", "தேங்காய் கொழுகட்டை"), Pair("Chilled Creamy Rabri Kulfi Cup", "ரப்ரி குல்பி"),
                Pair("Sweet Malpua Saffron Crepe", "மல்பூவா இனிப்பு"), Pair("Soft Sugar Syrup Rasgulla Cup", "ரஸ்குல்லா"),
                Pair("Rich Roasted Cashew Kaju Katli", "காஜு கத்லி"), Pair("Traditional Besan Gram Laddoo", "கடலை மாவு லட்டு"),
                Pair("Red Carrot Sweet Gajar Halwa", "கேரட் அல்வா"), Pair("Sweet Elaneer Milk Payasam", "இளநீர் பாயாசம்")
            )
            3 -> listOf(
                Pair("Handmade Crispy Kai Murukku", "கை முறுக்கு"), Pair("Spicy Roasted Madras Mixture", "மெட்ராஸ் மிக்சர்"),
                Pair("Peanut Curry Leaves Kara Boondhi", "கார பூந்தி"), Pair("Fennel Spiced Crunchy Thenkuzhal", "தேன்குழல் முறுக்கு"),
                Pair("Ajwain Digest Spiced Omapodi", "ஓமப்பொடி"), Pair("Coconut Oil Raw Banana Chips", "வாழைக்காய் சிப்ஸ்"),
                Pair("Thick Crunchy Spicy Potato Chips", "கார உருளை சிப்ஸ்"), Pair("Pepper Spiced Crispy Banana Chips", "மிளகு வாழை சிப்ஸ்"),
                Pair("Savoury Ribbon Pakoda Fritter", "ரிப்பன் பகோடா"), Pair("Garlic Spiced Kara Sev Pack", "பூண்டு காராசேவ்"),
                Pair("Curry Leaves Spiced Peanut Fry", "மசாலா கடலை"), Pair("Sweet Puffed Rice Aval Pori Pack", "அவல் பொரி"),
                Pair("Crunchy Garlic Butter Murukku", "பூண்டு முறுக்கு"), Pair("Deep Fried Spiced Seed Murukku", "சீடை உருண்டை"),
                Pair("Roasted Crispy Spiced Makhana", "மசாலா தாமரை விதை"), Pair("Sweet Wheat Maida Biscuits Box", "மைதா இனிப்பு சீவல்")
            )
            else -> listOf(
                Pair("Filter Coffee Sizzling Cup", "பில்டர் காபி"), Pair("Ginger Cardamom Herbal Tea", "இஞ்சி ஏலக்காய் டீ"),
                Pair("Hot Creamy Saffron Badam Milk", "சூடான பாதாம் பால்"), Pair("Cold Sweet Cardamom Rose Milk", "ரோஸ் மில்க்"),
                Pair("Fresh Tangy Mint Lime Juice", "புதினா எலுமிச்சை சாறு"), Pair("Spiced Buttermilk Neer Mor", "நீர் மோர்"),
                Pair("Churned Sweet Yogurt Lassi Cups", "மதுர லஸ்ஸி"), Pair("Healthy Ragi Malt Millet Milk", "கேழ்வரகு கஞ்சி"),
                Pair("Spiced Golden Turmeric Milk", "மஞ்சள் பால்"), Pair("Fresh Watermelon Cooling Pulp", "தர்பூசணி ஜூஸ்"),
                Pair("Tangy Nannari Sarbath Cool", "நன்னாரி சர்பத்"), Pair("Crushed Ice Rose Gola Slush", "ஐஸ் குச்சி ஐஸ்"),
                Pair("Cooling Basil seed Lemon Soda", "சப்ஜா லெமன் சோடா"), Pair("Hot Black Pepper Sukku Coffee", "சுக்கு மல்லி காபி"),
                Pair("Immunity Booster Ginger Tea", "சுக்கு சுடான டீ"), Pair("Traditional Elaneer Tender Water", "இயற்கை இளநீர்")
            )
        }
        val p = menu[idx]
        return DishTemplate(
            nameEn = p.first,
            nameTa = p.second,
            descEn = "Crispy and fresh traditional snack ${p.first} from local kitchens. Best enjoyed hot.",
            descTa = "மொறுமொறுப்பான பாரம்பரிய நொறுக்குத்தீனி ${p.second} மாலை நேரத்திற்கு மிகச் சிறந்தது.",
            price = 25.0 + idx * 6,
            isVeg = true,
            imgUrl = ""
        )
    }

    private fun getDhabaPresetItem(cat: Int, idx: Int): DishTemplate {
        val menu = when (cat) {
            0 -> listOf(
                Pair("Spicy Paneer Tikka Kebabs", "பனீர் டிக்கா கபாப்"), Pair("Tandoori Skewered Chicken Kebabs", "சிக்கன் கபாப்"),
                Pair("Hariyali Mint Chicken Kebabs", "ஹரியாலி சிக்கன் கபாப்"), Pair("Spicy Garlic Skewered Malai Tikka", "மலாய் கோழி கபாப்"),
                Pair("Charcoal Baked Mutton Seekh Kebab", "மட்டன் சீக் கபாப்"), Pair("Clay Oven Baked Tangri Legs Pair", "தந்தூரி லெக் பீஸ்"),
                Pair("Charcoal Spicy Potatoes Tikka", "ஆலூ டிக்கா கபாப்"), Pair("Spicy Ginger Garlic Tandoori Gobi", "தந்தூரி காலிபிளவர்"),
                Pair("Tandoori Soya Chaap Skewers", "சோயா சாப் கபாப்"), Pair("Clay Oven Baked Fish Tikka Kebab", "மீன் டிக்கா கபாப்"),
                Pair("Highway Egg Seekh Kebab Kebab", "முட்டை சீக் கபாப்"), Pair("Peri Peri Spiced Paneer Tikka", "நெருப்பு பனீர் டிக்கா"),
                Pair("Lemon Herbs Butter Chicken Tikka", "எலுமிச்சை கோழி கபாப்"), Pair("Hariyali Mint Paneer Tikka Green", "ஹரியாலி பனீர் கபாப்"),
                Pair("Spiced Achari Pickle Soya Chaap", "அச்சாரி சோயா கபாப்"), Pair("Dhaba Mixed Veg Charcoal Platter", "மிக்சர் வெஜ் கபாப்")
            )
            1 -> listOf(
                Pair("Punjabi Dal Makhani Black Lentil", "தால் மக்கானி"), Pair("Highway Spicy Kadai Paneer", "கடாய் பனீர்"),
                Pair("Dhaba Butter Chicken Cashew Masala", "தாபா பட்டர் சிக்கன்"), Pair("Highway Country Style Mutton Curry", "ஆட்டுக்கறி மசாலா"),
                Pair("Punjabi Chole Chickpeas Masala", "சென்னா மசாலா"), Pair("Dhaba Spicy Scrambled Egg Bhurji", "முட்டை புர்ஜி"),
                Pair("Golden Paneer Butter Bhurji Pan", "பனீர் புர்ஜி மசாலா"), Pair("Mix Veg Kolhapuri Extra Hot", "கோலாப்பூரி காய்கறி மசாலா"),
                Pair("Spicy Button Mushroom Do Pyaza", "காளான் மசாலா"), Pair("Rich Cashew Butter Shahi Paneer", "ஷாஹி பனீர் மசாலா"),
                Pair("Methi Malai Mattar Cream Gravy", "வெந்தய கீரை பட்டாணி மசாலா"), Pair("Spicy Yellow Dal Tadka Fry", "தால் தட்கா வறுவல்"), Pair("Aloo Jeera Dry Toss Potato", "சீரக உருளை வறுவல்"), Pair("Spicy Boiled Egg Curry Gravy", "முட்டை குழம்பு"), Pair("Punjabi Style Kadhi Pakora Sour", "மோர் பாஜி வடை குழம்பு"), Pair("Clay Pot Simmered Keema Masala", "கோழி கீமா மசாலா")
            )
            2 -> listOf(
                Pair("Basmati Jeera Ghee Steam Rice", "சீரக சாதம்"), Pair("Rich Fragrant Vegetable Pulao", "வெஜ் புலாவ்"), Pair("Dhaba Chicken Dum Biryani Pot", "தாபா கோழி பிரியாணி"), Pair("Egg Fried Basmati Rice Wok", "முட்டை பிரைடு ரைஸ்"), Pair("Royal Cashew Green Peas Pulao", "முந்திரி சாதம்"), Pair("Pure Steam Basmati Ghee Pot", "நெய் வடி சாதம்"), Pair("Dhaba Mutton Dum Biryani Pot", "தாபா ஆட்டுக்கறி பிரியாணி"), Pair("Dhaba Egg Dum Biryani Pot", "தாபா முட்டை பிரியாணி"), Pair("Fresh Mint Coriander Herb Rice", "புதினா சாதம்"), Pair("Golden Fried Garlic Basmati Rice", "கார்லிக் பிரைடு ரைஸ்"), Pair("Schezwan Spicy Chicken Rice Wok", "செஸ்வான் சிக்கன் சாதம்"), Pair("Paneer Golden Pulao Basmati", "பனீர் புலாவ் சாதம்"), Pair("Dhaba Style Dal Khichdi Bowl", "தால் கிச்சடி"), Pair("Spicy Kaju Biryani Cashew Royal", "முந்திரி பிரியாணி"), Pair("Curd Rice Dhaba Special Tadka", "தாபா தயிர் சாதம்"), Pair("Spicy Pepper Fried Rice Wok", "மிளகு சாதம்")
            )
            3 -> listOf(
                Pair("Charcoal Baked Butter Roti Wheat", "தந்தூரி வெண்ணெய் ரொட்டி"), Pair("Oven Baked Plain Wheat Naan", "சாதா நான்"),
                Pair("Garlic Herbs Butter Naan Giant", "கார்லிக் பட்டர் நான்"), Pair("Clay Oven Plain Wheat Roti", "தந்தூரி ரொட்டி"),
                Pair("Layered Wheat Lachha Paratha", "லச்சா பராத்தா"), Pair("Spicy Stuffed Potato Aloo Paratha", "ஆலூ பராத்தா"),
                Pair("Stuffed cottage Cheese Paneer Paratha", "பனீர் பராத்தா"), Pair("Fennel Spiced Keema stuffed Naan", "கீமா நான் ரொட்டி"),
                Pair("Cheese Loaded Butter Naan Giant", "சீஸ் பட்டர் நான்"), Pair("Spiced Mint Pudina Lachha Paratha", "புதினா பராத்தா"),
                Pair("Spicy Chili Garlic Charcoal Naan", "சில்லி கார்லிக் நான்"), Pair("Stuffed Onion Pyaza Kulcha Naan", "வெங்காய குல்சா"),
                Pair("Gram flour Spiced Missi Roti", "மிஸ்ஸி ரொட்டி"), Pair("Soft Clay Oven Rumali Roti", "ருமாலி மெல்லிய ரொட்டி"),
                Pair("Sweet Peshwari Cashew Raisin Naan", "காஷ்மீரி இனிப்பு நான்"), Pair("Traditional Highway Amritsari Kulcha", "அமிர்தசரஸ் குல்சா")
            )
            else -> listOf(
                Pair("Grand Punjabi Lassi Sweet Malai", "பஞ்சாபி லஸ்ஸி"), Pair("Chilled Ginger Mint Masala Chaas", "நீர் மோர்"),
                Pair("Saffron Pistachios Badam Lassi", "குங்குமப்பூ லஸ்ஸி"), Pair("Sweet Mango Alphonso Cream Lassi", "மாம்பழ லஸ்ஸி"),
                Pair("Chilled Saffron Milk Rasamalai Trio", "ரசமலாய்"), Pair("Hot Gulab Jamun Maple Sweet Duo", "குலாப் ஜாமுன்"),
                Pair("Creamy Basundi Rose Reduction Bowl", "பாசுந்தி"), Pair("Warm Carrot Halwa Cashew Crumble", "கேரட் அல்வா"),
                Pair("Strong Tandoori Chai Spiced Cup", "தந்தூரி டீ"), Pair("Clay Pot Chilled Matka Kulfi", "மதுபான குல்பி"),
                Pair("Pepsi Cola Carbonated Cold Can", "பெப்சி குவளை"), Pair("Fresh Lemon Mint Soda Squeeze", "லெமன் சோடா"),
                Pair("Creamy Fruit Salad Honey Dressing", "பழ சாலட்"), Pair("Spiced Saffron Milk Kheer Pot", "பால் பாயாசம்"),
                Pair("Milky Vanilla Cup Ice cream", "வெண்ணிலா ஐஸ்கிரீம்"), Pair("Mocha Coffee Hot Dhaba Style", "மசாலா காபி")
            )
        }
        val isVeg = (cat != 0 || idx == 0 || idx == 6 || idx == 7 || idx == 8 || idx == 11 || idx == 13 || idx == 15) &&
                    (cat != 1 || idx == 0 || idx == 1 || idx == 4 || idx == 6 || idx == 7 || idx == 8 || idx == 9 || idx == 10 || idx == 11 || idx == 12 || idx == 14) &&
                    (cat != 2 || idx != 2 && idx != 6 && idx != 10)
        val p = menu[idx]
        return DishTemplate(
            nameEn = p.first,
            nameTa = p.second,
            descEn = "Robust and flavorful Dhaba specialty ${p.first}, slow cooked on highway charcoal furnaces.",
            descTa = "தரமான சிறந்த தாபா முறையில் சமைக்கப்பட்ட உணவு ${p.second}.",
            price = 40.0 + idx * 12,
            isVeg = isVeg,
            imgUrl = ""
        )
    }

    private data class DishTemplate(
        val nameEn: String,
        val nameTa: String,
        val descEn: String,
        val price: Double,
        val isVeg: Boolean,
        val imgUrl: String,
        val descTa: String = ""
    )
}
