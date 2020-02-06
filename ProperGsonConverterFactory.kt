import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException

import java.io.IOException
import kotlin.Annotation
import java.lang.reflect.Type

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * @author Adam Howard
 * @since 15/05/2018
 * A Retrofit [Converter.Factory] providing [Converter]s that correctly convert:-
 * * Objects of the specified [Type] into a gson-encoded [RequestBody], and
 * * [Gson]-encoded JSON-String [ResponseBody] objects back into objects of the specified type.
 *
 * This implementation exists to fix the following problem in Retrofit's own
[GSON converter implementation](https://github.com/square/retrofit/tree/master/retrofit-converters/gson/src/main/java/retrofit2/converter/gson),
 * where subclass fields aren't serialised by Retrofit:
 *
 * Retrofit's own GSON Converter Factory uses some bizarre, old technique
 * (involving TypeAdapters, JsonWriters...) to turn a POJO into a string.
 *
 * This is now unnecessary, and disables modern GSON's clever serialisation of subclasses
 * (which also serialises fields only present in the subclass-instance of a type).
 *
 * @constructor Construct an instance.
 * @param gson the Gson object to use for conversion.
 */
internal class ProperGsonConverterFactory(private val gson:Gson) : Converter.Factory() {
    private val TAG = "ProperGsonConverterFac"

    override fun responseBodyConverter(type:Type, annotations:Array<Annotation>, retrofit:Retrofit):Converter<ResponseBody, out Any>? {
        return MyGsonResponseBodyConverter(gson, type)
    }

    override fun requestBodyConverter(type:Type, parameterAnnotations:Array<Annotation>, methodAnnotations:Array<Annotation>, retrofit:Retrofit):Converter<out Any, RequestBody>? {
        return MyGsonRequestBodyConverter(gson)
    }

    /**@constructor
     * @param gson the gson instance to use during conversion
     * @param type the Java Type to convert the JSON string back to*/
    private class MyGsonResponseBodyConverter<T>(private val gson:Gson, private val type:Type) : Converter<ResponseBody, T> {

        @Throws(IOException::class, JsonSyntaxException::class, JsonIOException::class)
        override fun convert(value:ResponseBody):T {
            try {
                val raw = value.string()
                //super-useful (but insecure) raw JSON response debugging line
                //Log.i(TAG, "raw response json: "+raw)
                return gson.fromJson(raw, type)
            }
            /* catch (JsonIOException jsonIoException){
                Log.e(TAG, "JSONIOException: "+jsonIoException)
            } catch (JsonSyntaxException jsonException){
                Log.e(TAG, "JSON Syntax Exception: "+jsonException)
            }*/
            finally {
                value.close()
            }
        }
    }

    private class MyGsonRequestBodyConverter<T>(private val gson:Gson) : Converter<T, RequestBody> {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")

        override fun convert(value:T):RequestBody? {
            //Writer writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
            return RequestBody.create(MEDIA_TYPE, gson.toJson(value))
        }
    }
}
