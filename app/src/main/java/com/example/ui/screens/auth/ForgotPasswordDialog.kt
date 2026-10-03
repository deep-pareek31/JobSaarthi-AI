package com.example.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (submitted) "Reset Link Sent" else "Reset Password",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (submitted) {
                    Text("If an account exists for $email, a secure password reset link has been dispatched to your email address.")
                } else {
                    Text("Enter your registered email address and we'll send instructions to reset your password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (submitted) {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            } else {
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            submitted = true
                        }
                    }
                ) {
                    Text("Send Link")
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
