import sys

file_path = 'app/src/main/java/com/example/ui/screens/AdminScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Save vendor changes (Line 2658)
target_1 = """                                                    viewModel.updateVendor(updated)
                                                  },"""
replacement_1 = """                                                    val localContext = context
                                                    viewModel.updateVendor(updated) {
                                                        android.widget.Toast.makeText(localContext, "✅ கடையின் விவரங்கள் சேமிக்கப்பட்டன! (Store details saved!)", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                  },"""

# 2. Delete Vendor (Line 2689)
target_2 = """                                                                 viewModel.deleteVendor(partner)"""
replacement_2 = """                                                                 val localContext = context
                                                                 viewModel.deleteVendor(partner) {
                                                                     android.widget.Toast.makeText(localContext, "🗑️ கடை முற்றிலும் நீக்கப்பட்டது! (Store deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                                                 }"""

# 3. Custom banner selected (Line 2758)
target_3 = """                                                         selectedBannerPreset = presetValue
                                                         val updated = partner.copy(bannerUrl = presetValue)
                                                         viewModel.updateVendor(updated)"""
replacement_3 = """                                                         selectedBannerPreset = presetValue
                                                         val updated = partner.copy(bannerUrl = presetValue)
                                                         val localContext = context
                                                         viewModel.updateVendor(updated) {
                                                             android.widget.Toast.makeText(localContext, "✅ பேனர் புதுப்பிக்கப்பட்டது! (Banner updated!)", android.widget.Toast.LENGTH_SHORT).show()
                                                         }"""

# 4. Toggle promo code enabled/disabled switch (Line 2786)
target_4 = """                                                 val updated = partner.copy(isCouponEnabled = it)
                                                 viewModel.updateVendor(updated)"""
replacement_4 = """                                                 val updated = partner.copy(isCouponEnabled = it)
                                                 val localContext = context
                                                 viewModel.updateVendor(updated) {
                                                     android.widget.Toast.makeText(localContext, if (it) "✅ கூப்பன் செயல்படுத்தப்பட்டது! (Coupon enabled!)" else "⚠️ கூப்பன் முடக்கப்பட்டது! (Coupon disabled!)", android.widget.Toast.LENGTH_SHORT).show()
                                                 }"""

# 5. Save promo code voucher button (Line 2835)
target_5 = """                                                 val updated = partner.copy(
                                                     isCouponEnabled = isCouponEnabled,
                                                     couponCode = couponCode.trim().uppercase(),
                                                     couponDiscount = couponDiscount.toDoubleOrNull() ?: 80.0,
                                                     couponMinOrder = couponMinOrder.toDoubleOrNull() ?: 300.0,
                                                     bannerUrl = selectedBannerPreset
                                                 )
                                                 viewModel.updateVendor(updated)"""
replacement_5 = """                                                 val updated = partner.copy(
                                                     isCouponEnabled = isCouponEnabled,
                                                     couponCode = couponCode.trim().uppercase(),
                                                     couponDiscount = couponDiscount.toDoubleOrNull() ?: 80.0,
                                                     couponMinOrder = couponMinOrder.toDoubleOrNull() ?: 300.0,
                                                     bannerUrl = selectedBannerPreset
                                                 )
                                                 val localContext = context
                                                 viewModel.updateVendor(updated) {
                                                     android.widget.Toast.makeText(localContext, "✅ கூப்பன் விவரங்கள் சேமிக்கப்பட்டன! (Coupon details saved!)", android.widget.Toast.LENGTH_SHORT).show()
                                                 }"""

# 6. Create category button (Line 2879)
target_6 = """                                        onClick = { viewModel.createCategory(partner.id) },"""
replacement_6 = """                                        onClick = {
                                            val localContext = context
                                            viewModel.createCategory(
                                                vendorId = partner.id,
                                                onSuccess = {
                                                    android.widget.Toast.makeText(localContext, "✅ வகை வெற்றிகரமாக உருவாக்கப்பட்டது! (Category created!)", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { msg ->
                                                    android.widget.Toast.makeText(localContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },"""

# 7. Add dish to menu (Line 3109)
target_7 = """                                             onClick = { viewModel.createMenuItem(partner.id) },"""
replacement_7 = """                                             onClick = {
                                                val localContext = context
                                                viewModel.createMenuItem(
                                                    vendorId = partner.id,
                                                    onSuccess = {
                                                        android.widget.Toast.makeText(localContext, "✅ உணவு வெற்றிகரமாக சேர்க்கப்பட்டது! (Dish added!)", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { msg ->
                                                        android.widget.Toast.makeText(localContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },"""

# 8. Save dish changes (Line 3655)
target_8 = """                                             viewModel.updateMenuItem(updated)
                                             editingMenuItem = null"""
replacement_8 = """                                             val localContext = context
                                             viewModel.updateMenuItem(updated) {
                                                 android.widget.Toast.makeText(localContext, "✅ உணவின் விவரங்கள் சேமிக்கப்பட்டன! (Dish updated!)", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                             editingMenuItem = null"""

# 9. Update category changes (Line 3730)
target_9 = """                                                 viewModel.updateCategory(
                                                     catNode.copy(
                                                         nameEn = nameEn.trim(),
                                                         nameTa = nameTa.trim(),
                                                         autoOpenTime = autoOpen.trim(),
                                                         autoCloseTime = autoClose.trim()
                                                     )
                                                 )
                                                 categoryToEdit = null"""
replacement_9 = """                                                 val localContext = context
                                                 viewModel.updateCategory(
                                                     catNode.copy(
                                                         nameEn = nameEn.trim(),
                                                         nameTa = nameTa.trim(),
                                                         autoOpenTime = autoOpen.trim(),
                                                         autoCloseTime = autoClose.trim()
                                                     )
                                                 ) {
                                                     android.widget.Toast.makeText(localContext, "✅ வகைப்பாடு சேமிக்கப்பட்டது! (Category saved!)", android.widget.Toast.LENGTH_SHORT).show()
                                                 }
                                                 categoryToEdit = null"""

# 10. Delete category (Line 3762)
target_10 = """                                         onClick = {
                                             viewModel.deleteCategory(catNode)
                                             categoryToDelete = null
                                         }"""
replacement_10 = """                                         onClick = {
                                             val localContext = context
                                             viewModel.deleteCategory(catNode) {
                                                 android.widget.Toast.makeText(localContext, "🗑️ வகைப்பாடு நீக்கப்பட்டது! (Category deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                             categoryToDelete = null
                                         }"""

# 11. Delete menu item (Line 3788)
target_11 = """                                         onClick = {
                                             viewModel.deleteMenuItem(dish)
                                             menuItemToDelete = null
                                         }"""
replacement_11 = """                                         onClick = {
                                             val localContext = context
                                             viewModel.deleteMenuItem(dish) {
                                                 android.widget.Toast.makeText(localContext, "🗑️ உணவு நீக்கப்பட்டது! (Dish deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                             menuItemToDelete = null
                                         }"""

replacements = [
    (target_1, replacement_1),
    (target_2, replacement_2),
    (target_3, replacement_3),
    (target_4, replacement_4),
    (target_5, replacement_5),
    (target_6, replacement_6),
    (target_7, replacement_7),
    (target_8, replacement_8),
    (target_9, replacement_9),
    (target_10, replacement_10),
    (target_11, replacement_11)
]

modified = content
for i, (tgt, rpl) in enumerate(replacements):
    if tgt in modified:
        modified = modified.replace(tgt, rpl)
        print(f"Applied replacement {i+1} successfully.")
    else:
        print(f"Replacement {i+1} target not found!")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(modified)
