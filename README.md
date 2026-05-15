# DES Encryption Project

This JavaFX application implements the Data Encryption Standard (DES) for learning purposes. It supports encrypting and decrypting typed text and supported files through the UI.

## Main Features

- Key expansion: generate and display all 16 DES round keys.
- Encrypt text: type plaintext, enter a DES key, and display ciphertext as hexadecimal text.
- Decrypt text: paste hexadecimal ciphertext, enter the same DES key, and recover plaintext.
- Encrypt files: load a supported file and save encrypted bytes using a `.des` extension.
- Decrypt files: load encrypted `.des` data and save the restored file.
- Generate random DES key: create a valid 16-character hexadecimal key in the UI and save it to `generated_des_key.txt`.
- Load key from file: choose a text file that contains a DES key.
- Send DES key by SMS using Twilio.
- Send ciphertext or encrypted files by email using Gmail SMTP.

SMS sends the DES key. Email sends the encrypted output.

## Supported Inputs

The key must be exactly 16 hexadecimal characters, for example:

```text
133457799BBCDFF1
```

Text input can be normal plaintext when encrypting. When decrypting text, the input must be hexadecimal ciphertext produced by the app.

Supported file types are:

```text
.txt, .docx, .png, .jpg, .jpeg, .mp3, .wav, .mp4, .des
```

Files are processed as raw bytes, so the same DES block logic works for text, images, documents, audio, and video.

## How to Use

1. Enter a 16-character hex key, load a key file, or click `Generate Key`.
2. To encrypt typed text, type the plaintext in the input field and click `Encrypt`.
3. To decrypt typed text, paste the hexadecimal ciphertext in the input field and click `Decrypt Text`.
4. To encrypt a file, click `Load File`, choose a supported file, then click `Encrypt` and choose where to save the `.des` output.
5. To decrypt a file, click `Load File`, choose an encrypted file, then click `Decrypt` and choose where to save the restored output.
6. To inspect the DES key schedule, enter a valid key and click `Show Key Expansion`.
7. To send the key by SMS, enter a recipient phone number and click `Send Key SMS`.
8. To send encrypted output by email, encrypt text or a file, enter a recipient email, then click `Send Ciphertext Email`.

When `Generate Key` is clicked, the latest generated key is saved in the project folder as:

```text
generated_des_key.txt
```

The file is overwritten each time, so it always contains the newest generated key.

## Twilio SMS Setup

The SMS feature sends the current DES key from the key field. The SMS body is:

```text
DES key: <key>
```

Before using `Send Key SMS`, open `TwilioConfig.java` and replace the placeholder values:

```java
public static final String ACCOUNT_SID = "PUT_ACCOUNT_SID_HERE";
public static final String AUTH_TOKEN = "PUT_AUTH_TOKEN_HERE";
public static final String FROM_PHONE = "PUT_TWILIO_PHONE_HERE";
```

Use your Twilio Account SID, Twilio Auth Token, and Twilio sender phone number.

Phone numbers must use international format:

```text
+970599123456
+12025550123
```

Twilio trial accounts may only send SMS messages to verified recipient phone numbers.

## Gmail Email Setup

The email feature sends encrypted output only:

- encrypted text is sent as hexadecimal ciphertext in the email body
- encrypted files are sent as `.des` attachments

Before using `Send Ciphertext Email`, open `EmailConfig.java` and replace the placeholder values:

```java
public static final String FROM_EMAIL = "PUT_GMAIL_HERE";
public static final String APP_PASSWORD = "PUT_GMAIL_APP_PASSWORD_HERE";
```

Use a Gmail App Password, not your normal Gmail password.

To create a Gmail App Password:

1. Enable 2-Step Verification on the Gmail account.
2. Open Google Account security settings.
3. Create an App Password for mail.
4. Copy that generated password into `EmailConfig.APP_PASSWORD`.

The recipient email is typed in the UI.

## DES Implementation Notes

The DES implementation includes:

- Initial permutation and final permutation.
- DES key schedule using PC-1, round shifts, and PC-2.
- 16 Feistel rounds.
- Expansion permutation from 32 bits to 48 bits.
- XOR with the round key.
- S-box substitution from 48 bits to 32 bits.
- P permutation.
- Reversed round keys for decryption.
- PKCS5 padding for data that is not already a full 8-byte block.

## Error Handling

The UI shows friendly messages for common problems:

- Missing key.
- Invalid key length or non-hex key characters.
- Missing text or file input.
- Unsupported file types.
- Invalid hexadecimal ciphertext.
- Invalid encrypted file size.
- Wrong key or invalid decrypted padding.

## Running the Project

This is a Maven JavaFX project. Run it from the project folder with:

```powershell
.\mvnw.cmd javafx:run
```

The Maven configuration currently targets Java 21.
