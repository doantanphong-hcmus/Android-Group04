package com.tangkinhcac.app.data.remote.auth

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthApiModelsTest {
    private val gson = Gson()

    @Test
    fun `register request uses documented snake case field names`() {
        val request =
            RegisterApiRequest(
                fullName = "Doan Tan Phong",
                username = "TanPhong",
                birthDate = "2000-01-01",
                phoneNumber = "+84912345678",
                email = "phong@example.com",
                password = "password123",
                passwordConfirmation = "password123",
            )

        val json = gson.toJson(request)

        assertTrue(json.contains("\"full_name\""))
        assertTrue(json.contains("\"birth_date\""))
        assertTrue(json.contains("\"phone_number\""))
        assertTrue(json.contains("\"password_confirmation\""))
        assertFalse(json.contains("\"fullName\""))
    }

    @Test
    fun `login response reads documented token and user envelope`() {
        val json =
            """
            {
              "data": {
                "token": "opaque-token",
                "token_type": "Bearer",
                "user": {
                  "id": 7,
                  "full_name": "Doan Tan Phong",
                  "username": "TanPhong",
                  "birth_date": "2000-01-01",
                  "phone_number": "+84912345678",
                  "email": "phong@example.com",
                  "role": "user"
                }
              }
            }
            """.trimIndent()

        val response = gson.fromJson(json, LoginApiResponse::class.java)

        assertEquals("opaque-token", response.data.token)
        assertEquals("Bearer", response.data.tokenType)
        assertEquals("Doan Tan Phong", response.data.user.fullName)
        assertEquals(7L, response.data.user.id)
    }
}
