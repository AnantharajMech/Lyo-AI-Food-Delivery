package com.example.data.repository

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val role: String) : AuthResult()
    object InvalidCredentials : AuthResult()
    object AccountNotFound : AuthResult()
    object WrongRole : AuthResult()
    object RiderInactive : AuthResult()
    object ProfileMissing : AuthResult()
    object NetworkError : AuthResult()
    object FirebaseConfigError : AuthResult()
    data class UnknownError(val message: String) : AuthResult()

    fun getErrorMessage(portal: String = "CUSTOMER"): String {
        return when (this) {
            is Loading -> "Authenticating... Please wait. (சரிபார்க்கப்படுகிறது... தயவுசெய்து காத்திருக்கவும்.)"
            is Success -> ""
            is InvalidCredentials -> "Incorrect mobile number or password. (தவறான கைபேசி எண் அல்லது கடவுச்சொல்.)"
            is AccountNotFound -> "Account not found. Please register first. (கணக்கு கண்டறியப்படவில்லை. தயவுசெய்து முதலில் பதிவு செய்யவும்.)"
            is WrongRole -> {
                when (portal) {
                    "ADMIN" -> "This account does not have Admin access. (இந்த கணக்கிற்கு நிர்வாகி அனுமதி இல்லை.)"
                    "RIDER", "DELIVERY" -> "This account does not have Rider access. (இந்த கணக்கிற்கு ரைடர் அனுமதி இல்லை.)"
                    else -> "Access Denied: Admin/Rider accounts cannot log in through this portal. (அனுமதி மறுக்கப்பட்டது: நிர்வாகி அல்லது ரைடர் கணக்குகள் இந்த போர்ட்டலில் உள்நுழைய முடியாது.)"
                }
            }
            is RiderInactive -> "Your rider account is currently inactive. Contact admin. (உங்கள் ரைடர் கணக்கு தற்போது முடக்கப்பட்டுள்ளது. நிர்வாகியைத் தொடர்பு கொள்ளவும்.)"
            is ProfileMissing -> "Account profile is incomplete. Please contact support. (கணக்கு சுயவிவரம் முழுமையடையவில்லை. ஆதரவை தொடர்பு கொள்ளவும்.)"
            is NetworkError -> "Network connection error. Please check your internet. (இணைய இணைப்பு பிழை. உங்கள் இணையத்தை சரிபார்க்கவும்.)"
            is FirebaseConfigError -> "System configuration error. Contact support. (அமைப்பு கட்டமைப்பு பிழை. ஆதரவைத் தொடர்பு கொள்ளவும்.)"
            is UnknownError -> "An unknown error occurred: $message (தெரியாத பிழை ஏற்பட்டது.)"
        }
    }
}
