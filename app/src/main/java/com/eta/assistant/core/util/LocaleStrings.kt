package com.eta.assistant.core.util

data class AppLanguage(
    val code: String,
    val name: String,
    val nativeName: String
)

object LocaleStrings {

    val supportedLanguages = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("ta", "Tamil", "தமிழ்"),
        AppLanguage("bn", "Bengali", "বাংলা")
    )

    fun getLanguageName(code: String): String {
        return supportedLanguages.find { it.code == code }?.let { "${it.name} (${it.nativeName})" } ?: "English"
    }

    fun get(key: String, lang: String = "en"): String {
        val strings = when (lang) {
            "hi" -> hindiStrings
            "ta" -> tamilStrings
            "bn" -> bengaliStrings
            else -> englishStrings
        }
        return strings[key] ?: englishStrings[key] ?: key
    }

    private val englishStrings = mapOf(
        "app_title" to "Eta",
        "tagline" to "Your Offline Business Assistant",
        "home" to "Home",
        "inventory" to "Inventory",
        "customers" to "Customers",
        "reports" to "Reports",
        "settings" to "Settings",
        "more" to "More",
        "todays_overview" to "Today's Overview",
        "sales" to "Sales",
        "dues" to "Dues",
        "items_sold" to "Items Sold",
        "low_stock" to "Low Stock",
        "quick_actions" to "Quick Actions",
        "record_sale_voice" to "Record Sale\n(Voice)",
        "add_product" to "Add Product",
        "view_dues" to "View Dues",
        "recent_transactions" to "Recent Transactions",
        "offline" to "Offline",
        "good_morning" to "Good Morning,",
        "good_afternoon" to "Good Afternoon,",
        "good_evening" to "Good Evening,",
        "tap_to_speak" to "Tap to speak",
        "listening" to "Listening... speak now",
        "confirm_sale" to "Confirm Sale",
        "save_sale" to "Save Sale",
        "language" to "Language",
        "notifications" to "Notifications",
        "send_test_notification" to "Send Test Notification",
        "notification_enabled" to "Enabled",
        "notification_disabled" to "Disabled",
        "theme" to "Theme",
        "add_payment" to "Add Payment",
        "send_reminder" to "Send Reminder",
        "sync_backup" to "Sync & Backup",
        "business_info" to "Business Info",
        "total_due" to "Total Due",
        "search_products" to "Search products...",
        "search_customers" to "Search customer...",
        "view_inventory" to "View Inventory"
    )

    private val hindiStrings = mapOf(
        "app_title" to "ईटा",
        "tagline" to "आपका ऑफ़लाइन बिज़नेस सहायक",
        "home" to "होम",
        "inventory" to "इन्वेंटरी",
        "customers" to "ग्राहक",
        "reports" to "रिपोर्ट्स",
        "settings" to "सेटिंग्स",
        "more" to "अधिक",
        "todays_overview" to "आज का अवलोकन",
        "sales" to "बिक्री",
        "dues" to "बकाया",
        "items_sold" to "बेचे गए सामान",
        "low_stock" to "कम स्टॉक",
        "quick_actions" to "त्वरित क्रियाएं",
        "record_sale_voice" to "बिक्री दर्ज करें\n(आवाज़)",
        "add_product" to "उत्पाद जोड़ें",
        "view_dues" to "बकाया देखें",
        "recent_transactions" to "हाल के लेन-देन",
        "offline" to "ऑफ़लाइन",
        "good_morning" to "सुप्रभात,",
        "good_afternoon" to "शुभ दोपहर,",
        "good_evening" to "शुभ संध्या,",
        "tap_to_speak" to "बोलने के लिए टैप करें",
        "listening" to "सुन रहे हैं... अब बोलें",
        "confirm_sale" to "बिक्री की पुष्टि करें",
        "save_sale" to "बिक्री सहेजें",
        "language" to "भाषा",
        "notifications" to "सूचनाएं",
        "send_test_notification" to "परीक्षण सूचना भेजें",
        "notification_enabled" to "सक्रिय",
        "notification_disabled" to "निष्क्रिय",
        "theme" to "थीम",
        "add_payment" to "भुगतान जोड़ें",
        "send_reminder" to "याद दिलाएं",
        "sync_backup" to "सिंक और बैकअप",
        "business_info" to "व्यापार जानकारी",
        "total_due" to "कुल बकाया",
        "search_products" to "उत्पाद खोजें...",
        "search_customers" to "ग्राहक खोजें...",
        "view_inventory" to "इन्वेंटरी देखें"
    )

    private val tamilStrings = mapOf(
        "app_title" to "ஈடா",
        "tagline" to "உங்கள் ஆஃப்லைன் வணிக உதவியாளர்",
        "home" to "முகப்பு",
        "inventory" to "இருப்பு",
        "customers" to "வாடிக்கையாளர்கள்",
        "reports" to "அறிக்கைகள்",
        "settings" to "அமைப்புகள்",
        "more" to "மேலும்",
        "todays_overview" to "இன்றைய கண்ணோட்டம்",
        "sales" to "விற்பனை",
        "dues" to "நிலுவை",
        "items_sold" to "விற்பனையான பொருட்கள்",
        "low_stock" to "குறைந்த இருப்பு",
        "quick_actions" to "விரைவுச் செயல்கள்",
        "record_sale_voice" to "விற்பனை பதிவு\n(குரல்)",
        "add_product" to "பொருளைச் சேர்",
        "view_dues" to "நிலுவையைப் பார்",
        "recent_transactions" to "சமீபத்திய பரிவர்த்தனைகள்",
        "offline" to "ஆஃப்லைன்",
        "good_morning" to "காலை வணக்கம்,",
        "good_afternoon" to "மதிய வணக்கம்,",
        "good_evening" to "மாலை வணக்கம்,",
        "tap_to_speak" to "பேச தட்டவும்",
        "listening" to "கேட்கிறது... இப்போது பேசுங்கள்",
        "confirm_sale" to "விற்பனையை உறுதிப்படுத்து",
        "save_sale" to "விற்பனையைச் சேமி",
        "language" to "மொழி",
        "notifications" to "அறிவிப்புகள்",
        "send_test_notification" to "சோதனை அறிவிப்பை அனுப்பு",
        "notification_enabled" to "செயலில் உள்ளது",
        "notification_disabled" to "முடக்கப்பட்டுள்ளது",
        "theme" to "தீம்",
        "add_payment" to "பணம் செலுத்துதல் சேர்",
        "send_reminder" to "நினைவூட்டல் அனுப்பு",
        "sync_backup" to "ஒத்திசைவு மற்றும் காப்புப்பிரதி",
        "business_info" to "வணிகத் தகவல்",
        "total_due" to "மொத்த நிலுவை",
        "search_products" to "பொருட்களைத் தேடுங்கள்...",
        "search_customers" to "வாடிக்கையாளரைத் தேடுங்கள்...",
        "view_inventory" to "இருப்பைப் பார்க்கவும்"
    )

    private val bengaliStrings = mapOf(
        "app_title" to "ইটা",
        "tagline" to "আপনার অফলাইন ব্যবসায়িক সহকারী",
        "home" to "হোম",
        "inventory" to "ইনভেন্টরি",
        "customers" to "গ্রাহক",
        "reports" to "রিপোর্ট",
        "settings" to "সেটিংস",
        "more" to "আরও",
        "todays_overview" to "আজকের ওভারভিউ",
        "sales" to "বিক্রয়",
        "dues" to "বকেয়া",
        "items_sold" to "বিক্রি হওয়া পণ্য",
        "low_stock" to "কম স্টক",
        "quick_actions" to "দ্রুত অ্যাকশন",
        "record_sale_voice" to "বিক্রয় রেকর্ড\n(ভয়েস)",
        "add_product" to "পণ্য যোগ করুন",
        "view_dues" to "বকেয়া দেখুন",
        "recent_transactions" to "সাম্প্রতিক লেনদেন",
        "offline" to "অফলাইন",
        "good_morning" to "শুভ সকাল,",
        "good_afternoon" to "শুভ অপরাহ্ন,",
        "good_evening" to "শুভ সন্ধ্যা,",
        "tap_to_speak" to "কথা বলতে ট্যাপ করুন",
        "listening" to "শুনছি... এখন বলুন",
        "confirm_sale" to "বিক্রয় নিশ্চিত করুন",
        "save_sale" to "বিক্রয় সংরক্ষণ করুন",
        "language" to "ভাষা",
        "notifications" to "বিজ্ঞপ্তি",
        "send_test_notification" to "পরীক্ষা বিজ্ঞপ্তি পাঠান",
        "notification_enabled" to "সক্রিয়",
        "notification_disabled" to "নিষ্ক্রিয়",
        "theme" to "থিম",
        "add_payment" to "পেমেন্ট যোগ করুন",
        "send_reminder" to "স্মারক পাঠান",
        "sync_backup" to "সিঙ্ক এবং ব্যাকআপ",
        "business_info" to "ব্যবসায়িক তথ্য",
        "total_due" to "মোট বকেয়া",
        "search_products" to "পণ্য অনুসন্ধান করুন...",
        "search_customers" to "গ্রাহক অনুসন্ধান করুন...",
        "view_inventory" to "ইনভেন্টরি দেখুন"
    )
}

