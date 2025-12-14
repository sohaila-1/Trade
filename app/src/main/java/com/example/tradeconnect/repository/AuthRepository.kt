//package com.example.tradeconnect.repository
//
//import com.example.tradeconnect.model.AppUser
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.FirebaseFirestore
//
//
//
//class AuthRepository : (
//    private val auth: FirebaseAuth
//) : IAuthRepository {
//
//    override fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
//
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnSuccessListener { authResult ->
//
//                val uid = authResult.user?.uid ?: return@addOnSuccessListener
//
//                // 🔥 User minimal pour Firestore
//                val newUser = AppUser(
//                    uid = uid,
//                    username = email.substringBefore("@"),
//                    email = email,
//                    mobile = "",
//                    profileImageUrl = ""
//                )
//
//                // 🔥 Sauvegarde dans Firestore
//                FirebaseFirestore.getInstance()
//                    .collection("users")
//                    .document(uid)
//                    .set(newUser)
//                    .addOnSuccessListener {
//                        onResult(true, null)
//                    }
//                    .addOnFailureListener { e ->
//                        onResult(false, e.message)
//                    }
//            }
//            .addOnFailureListener { e ->
//                onResult(false, e.message)
//            }
//    }
//
//
//    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnSuccessListener {
//                onResult(true, null)
//            }
//            .addOnFailureListener {
//                onResult(false, it.message)
//            }
//    }
//
//    override fun logout() {
//        auth.signOut()
//    }
//
//    override fun getCurrentUser(): FirebaseUser? {
//        return auth.currentUser
//    }
//
//    override fun getCurrentUserModel(): AppUser? {
//        val user = auth.currentUser ?: return null
//
//        return AppUser(
//            uid = user.uid,
//            username = user.email?.substringBefore("@") ?: "unknown"
//        )
//    }
//}
