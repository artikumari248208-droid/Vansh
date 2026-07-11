package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGray
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.NearBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.ChatViewModel

enum class OnboardingState {
    WELCOME,
    PHONE_INPUT,
    OTP_VERIFICATION
}

@Composable
fun WelcomeScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var screenState by remember { mutableStateOf(OnboardingState.WELCOME) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Decorative ambient background blur representing a minimal starfield or glowing AI core
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(150.dp))
                .blur(80.dp)
        )

        when (screenState) {
            OnboardingState.WELCOME -> {
                WelcomeOptions(
                    onPhoneClick = { screenState = OnboardingState.PHONE_INPUT },
                    onGoogleClick = {
                        Toast.makeText(context, "Continuing with Google Sign-In...", Toast.LENGTH_SHORT).show()
                        viewModel.login("Google Sathi", "9876543210")
                    },
                    onAppleClick = {
                        Toast.makeText(context, "Continuing with Apple Sign-In...", Toast.LENGTH_SHORT).show()
                        viewModel.login("Apple Sathi", "9876543210")
                    }
                )
            }
            OnboardingState.PHONE_INPUT -> {
                PhoneInputView(
                    phoneNumber = phoneNumber,
                    onPhoneChange = { if (it.length <= 10) phoneNumber = it },
                    onBackClick = { screenState = OnboardingState.WELCOME },
                    onGetOTPClick = {
                        if (phoneNumber.length == 10) {
                            screenState = OnboardingState.OTP_VERIFICATION
                            Toast.makeText(context, "OTP Sent! Try entering: 123456", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            OnboardingState.OTP_VERIFICATION -> {
                OtpVerificationView(
                    phoneNumber = phoneNumber,
                    otpCode = otpCode,
                    onOtpChange = { if (it.length <= 6) otpCode = it },
                    userName = userName,
                    onNameChange = { userName = it },
                    onBackClick = { screenState = OnboardingState.PHONE_INPUT },
                    onVerifyClick = {
                        if (userName.isBlank()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        } else if (otpCode != "123456") {
                            Toast.makeText(context, "Incorrect OTP! Use the test code: 123456", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Welcome, $userName!", Toast.LENGTH_SHORT).show()
                            viewModel.login(userName, phoneNumber)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeOptions(
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large Premium Wordmark
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Hello",
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-2).sp,
                modifier = Modifier.testTag("app_wordmark")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "INDIAN AI ASSISTANT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                letterSpacing = 4.sp
            )
        }

        // Action Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect securely to begin conversation",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Phone Login (Primary Monochrome)
            Button(
                onClick = onPhoneClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_phone_button"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Phone Number",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Google Button (Outline Monochrome)
            Button(
                onClick = onGoogleClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_google_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Simple Google Custom Character 'G' to match B&W luxury style
                    Text(
                        text = "G",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Apple Button (Outline Monochrome)
            Button(
                onClick = onAppleClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_apple_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Apple icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Apple ID",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom legal
        Text(
            text = "Designed for India. Optimized for NCERT solutions.",
            color = AccentGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun PhoneInputView(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onGetOTPClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Enter phone number",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We will send a 6-digit confirmation code.",
            fontSize = 15.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Phone Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🇮🇳 +91",
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                placeholder = { Text("00000 00000", color = AccentGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = BorderDark,
                    cursorColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("phone_input_field")
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGetOTPClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = PureBlack
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("get_otp_button")
        ) {
            Text(
                text = "Get OTP",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OtpVerificationView(
    phoneNumber: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    userName: String,
    onNameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Verify Details",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Code sent to +91 $phoneNumber. Test code is 123456",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Username Field (Required for Hello, [Name] greeting)
        Text(
            text = "What should we call you?",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            placeholder = { Text("Enter your name", color = AccentGray) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = PureWhite,
                unfocusedBorderColor = BorderDark,
                cursorColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("name_input_field")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // OTP Code Field
        Text(
            text = "6-Digit OTP",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = onOtpChange,
            placeholder = { Text("Enter 123456", color = AccentGray) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = PureWhite,
                unfocusedBorderColor = BorderDark,
                cursorColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("otp_input_field")
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVerifyClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = PureBlack
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("verify_otp_button")
        ) {
            Text(
                text = "Verify & Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
