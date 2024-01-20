package ru.deltadelete.lab14

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.LoginBody
import ru.deltadelete.lab14.api.RegisterBody
import ru.deltadelete.lab14.api.RetrofitClient
import ru.deltadelete.lab14.api.User
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun logging_in_as_user() {
        // Arrange
        val loginBody = LoginBody(
            "r.voronin@deltadelete.ru",
            "password"
        )
        val sud = Common.loginService

        // Act
        val call = sud.login(loginBody)

        // Assert
        try {
            val response = call.execute()

            val body = response.body()
            println(body)
            assertNotNull(body)

            val cookie = response.headers().get("set-cookie")
            println(cookie)
            assertNotNull(cookie)

        } catch (e: Exception) {
            throw e
        }
    }

    @Test
    fun register_new_user() {
        val registerBody = RegisterBody(
            "Юзеров",
            "Юзер",
            SimpleDateFormat("MM/dd/yyyy").parse("10/12/2004"),
            "user@example.com",
            "password",
            "password",
        )
        val sud = Common.loginService

        val call = sud.register(registerBody)

        try {
            val response = call.execute()
            val body = response.body()
            println(body)
                assertNotNull(body)
        } catch (e: Exception) {
            throw e
        }
    }
}