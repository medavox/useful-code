
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


/**
 * @author Adam Howard
 * @since 25/09/2018
 */
class SmsReceiver : BroadcastReceiver() {
    private enum class ValidCommands(val text:String) {
        LOCKDOWN("LOCKDOWN"),
        UNLOCK("UNLOCK")

    }
    private val TAG = "SmsReceiver"
    private val TRUSTED_SENDER_NAME = "Elucid"

    /**In Kotlin, you can't use extension functions to override existing functions;
     * in this case, toString(). So instead, we use this function.*/
    private fun SmsMessage.asString() :String {
        return "from=$originatingAddress; "+
                "body={$messageBody}; "
    }

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        //Only <=768-bit signatures can fit into a single SMS message (when base64 encoded),
        // and leave room for a message
        //768 bits = 96 bytes
        //96 bytes encoded to base64 = 128 characters, out of the 140-character limit for SMSes
        generator.initialize(768, SecureRandom())

        return generator.generateKeyPair()
    }

    fun sign(plainText: String, privateKey: PrivateKey): ByteArray {
        val privateSignature = Signature.getInstance("SHA256withRSA")
        privateSignature.initSign(privateKey)
        privateSignature.update(plainText.toByteArray())

        return privateSignature.sign()
    }


    fun verify(plainText: String, signature: ByteArray, publicKey: PublicKey): Boolean {
        val publicSignature = Signature.getInstance("SHA256withRSA")
        publicSignature.initVerify(publicKey)
        publicSignature.update(plainText.toByteArray())

        return publicSignature.verify(signature)
    }
    /*METHODOLOGY:
    The server sends an SMS containing a command,
        and the signature of a larger, unsent message.

    The larger message contained unique IDs for the app user --
        which both the app and the server know -- and the command.

    The app can reconstruct the larger (unsent) message using its own local data
        (and the command that was sent with the signature) and verify that.

    The signature won't verify if the data that was signed and the app-local data don't match
    */
    override fun onReceive(context: Context?, intent: Intent?) {

        if(context != null && intent != null) {
            // Get received SMS array
            val texts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for(text in texts) {
                if(text.originatingAddress == TRUSTED_SENDER_NAME) {
                    Log.i(TAG, "Received a text, claiming to be from HQ. Verifying...")

                    val commandsChoiceRegex = ValidCommands.values().foldIndexed("(")
                        {i, acc, cmd -> acc+if(i != 0){"|"}else{""}+cmd}+")"

                    //one of the set commands, a space, then 128 characters of signature in base64
                    val validCommandWithSignatureRegex = Regex(
                            "$commandsChoiceRegex ([A-Za-z0-9+/]{126,128}={0,2})")

                    val isValidCommandTextFormat = text.messageBody
                            .matches(validCommandWithSignatureRegex)
                    if(!isValidCommandTextFormat){
                        Log.e(TAG, "received text is not in a valid format!\n" +
                                "Is someone trying to impersonate HQ??")
                    }
                    val sentCommand = validCommandWithSignatureRegex.replace(text.messageBody, "$1")
                    val signatureBase64 = validCommandWithSignatureRegex.replace(text.messageBody, "$2")
                    val signature = Base64.decode(signatureBase64, Base64.DEFAULT)
                    val publicSignature = Signature.getInstance("SHA256withRSA")
                    try {
                        val verifyKeyEncoded = "placeholder"
                        val orgId = "placeholder"
                        val trialId = "placeholder"
                        val userId = "placeholder"

                        val publicKeyBytes = Base64.decode(verifyKeyEncoded, Base64.DEFAULT)

                        //val publicKey:PublicKey =

                        //from https://stackoverflow.com/a/22077915
                        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
                        val publicKey = kf.generatePublic(X509EncodedKeySpec(publicKeyBytes))

                        publicSignature.initVerify(publicKey)
                        //ORDERING IS IMPORTANT HERE!
                        publicSignature.update(sentCommand.toByteArray())
                        publicSignature.update(orgId.toByteArray())
                        publicSignature.update(trialId.toByteArray())
                        publicSignature.update(userId.toByteArray())

                        val isVerified = publicSignature.verify(signature)
                        if(isVerified) {
                            Log.i(TAG, "LOCKDOWN! verified lockdown command received.\n" +
                                    "entering lockdown mode...")
                        }
                    }catch(rnfe: Resources.NotFoundException) {
                        Log.e(TAG, "verify-key for server was not stored in SP!\n" +
                                "Can't verify received SMS command! Ignoring it instead.")
                        return
                    }
                }
            }
        }
    }
}
