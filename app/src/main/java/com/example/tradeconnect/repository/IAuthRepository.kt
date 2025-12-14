import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseUser

interface IAuthRepository {



    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        username: String,
        onResult: (Boolean, String?) -> Unit
    )


    fun logout()

    fun getCurrentUser(): FirebaseUser?

    fun getCurrentUserModel(): AppUser?
}
