package com.example.wizardlydo.screens.donation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wizardlydo.R
import com.example.wizardlydo.comps.textFile.readDonationText
import com.example.wizardlydo.comps.textFile.readPayPalUsername
import com.example.wizardlydo.data.currency.Currency
import com.example.wizardlydo.screens.donation.comps.QuickAmountButton
import com.example.wizardlydo.ui.theme.WizardlyDoTheme

@Composable
fun DonationScreen(
    onBack: () -> Unit
) {
    DonationContent(onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val paypalUsername = remember {
        readPayPalUsername(context)
    }

    val donationText = remember { readDonationText(context) }
    val titleText =
        if (donationText.isNotEmpty()) donationText[0] else "Thank you for supporting our project!"
    val descriptionText =
        if (donationText.size > 1) donationText[1] else "Your donation helps us continue developing amazing features and maintaining this app."

    var donationAmount by remember { mutableStateOf("10.00") }
    var showAmountError by remember { mutableStateOf(false) }

    val currencies = listOf(
        Currency("USD", "$", "US Dollar"),
        Currency("EUR", "€", "Euro"),
        Currency("GBP", "£", "British Pound"),
        Currency("JPY", "¥", "Japanese Yen"),
        Currency("CAD", "C$", "Canadian Dollar"),
        Currency("AUD", "A$", "Australian Dollar")
    )
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }
    var isCurrencyMenuExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val paypalBlue = Color(0xFF0070BA)
    val backgroundColor = Color(0xFFF7F9FA)

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val horizontalPadding = (screenWidth * 0.05f).coerceIn(12f, 24f).dp
    val verticalSpacing = (screenHeight * 0.02f).coerceIn(8f, 32f).dp
    val buttonHeight = (screenHeight * 0.07f).coerceIn(48f, 64f).dp

    val titleSize = (screenWidth * 0.05f).coerceIn(18f, 24f).sp
    val descriptionSize = (screenWidth * 0.04f).coerceIn(14f, 18f).sp
    val buttonTextSize = (screenWidth * 0.045f).coerceIn(16f, 20f).sp
    val labelSize = (titleSize.value * 0.9f).sp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Our Project") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(verticalSpacing))

            Image(
                painter = painterResource(id = R.drawable.paypal_logo),
                contentDescription = "PayPal Logo",
                modifier = Modifier.height((screenHeight * 0.06f).coerceIn(40f, 60f).dp)
            )

            Spacer(modifier = Modifier.height(verticalSpacing))

            Text(
                text = titleText,
                fontSize = titleSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(verticalSpacing / 2))

            Text(
                text = descriptionText,
                fontSize = descriptionSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = horizontalPadding / 2)
            )

            Spacer(modifier = Modifier.height(verticalSpacing))

            // Currency Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Currency:",
                    fontSize = labelSize,
                    fontWeight = FontWeight.Medium
                )

                Box {
                    OutlinedButton(
                        onClick = { isCurrencyMenuExpanded = true },
                        modifier = Modifier.width((screenWidth * 0.4f).coerceIn(120f, 200f).dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "${selectedCurrency.symbol} ${selectedCurrency.code}")
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Currency"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isCurrencyMenuExpanded,
                        onDismissRequest = { isCurrencyMenuExpanded = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text("${currency.symbol} ${currency.code} - ${currency.name}") },
                                onClick = {
                                    selectedCurrency = currency
                                    isCurrencyMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing / 2))

            Text(
                text = "Enter Donation Amount",
                fontSize = labelSize,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(verticalSpacing / 2))

            OutlinedTextField(
                value = donationAmount,
                onValueChange = { newValue ->
                    // Only allow valid currency format
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*(\\.\\d{0,2})?\$"))) {
                        donationAmount = newValue
                        showAmountError = false
                    }
                },
                label = { Text("Amount") },
                prefix = { Text(selectedCurrency.symbol) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = showAmountError,
                supportingText = {
                    if (showAmountError) {
                        Text("Please enter a valid amount")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(verticalSpacing))

            if (screenWidth > 600) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAmountButton(amount = "5.00") { donationAmount = it }
                    QuickAmountButton(amount = "10.00") { donationAmount = it }
                    QuickAmountButton(amount = "25.00") { donationAmount = it }
                    QuickAmountButton(amount = "50.00") { donationAmount = it }
                    QuickAmountButton(amount = "100.00") { donationAmount = it }
                    QuickAmountButton(amount = "Custom") { donationAmount = it }
                }
            } else {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAmountButton(amount = "5.00") { donationAmount = it }
                    QuickAmountButton(amount = "10.00") { donationAmount = it }
                    QuickAmountButton(amount = "25.00") { donationAmount = it }
                }

                Spacer(modifier = Modifier.height((screenHeight * 0.01f).coerceIn(4f, 12f).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAmountButton(amount = "50.00") { donationAmount = it }
                    QuickAmountButton(amount = "100.00") { donationAmount = it }
                    QuickAmountButton(amount = "Custom") { donationAmount = it }
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

            Button(
                onClick = {
                    if (donationAmount.isEmpty() || donationAmount.toFloatOrNull() == null || donationAmount.toFloat() <= 0) {
                        showAmountError = true
                    } else {
                        // Open PayPal.me link with the amount
                        val amount = donationAmount.toFloat()
                        val paypalUri = Uri.parse("https://www.paypal.me/$paypalUsername/$amount/${selectedCurrency.code}")
                        val intent = Intent(Intent.ACTION_VIEW, paypalUri)
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .padding(horizontal = horizontalPadding),
                colors = ButtonDefaults.buttonColors(
                    containerColor = paypalBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Donate with PayPal",
                    fontSize = buttonTextSize,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(verticalSpacing * 3 / 4))

            Text(
                text = "You will be redirected to PayPal to complete your donation securely in ${selectedCurrency.name}.",
                fontSize = (descriptionSize.value * 0.9f).sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )

            Spacer(modifier = Modifier.height(verticalSpacing))
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DonationContentPreview() {
    WizardlyDoTheme {
        DonationContent(
            onBack = {}
        )
    }
}

